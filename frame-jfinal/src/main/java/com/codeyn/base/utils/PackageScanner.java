package com.codeyn.base.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.asm.ClassReader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ClassUtils;

/**
 * Package scanner
 * not only local class also provide third jars, without jre
 * 
 * provide ant style match
 * @author Arthur
 *
 */
public class PackageScanner{

    private static final Log logger = LogFactory.getLog(PackageScanner.class);
    
    private static final String DOT = ".";
    private static final String LL_DOT = "\\.";
    
    private static ResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
    
    public interface ClassFilter{
        boolean access(Class<?> clazz);
    }
    
    public static Set<Class<?>> getClasses(String name) {
        return getClasses(name, true);
    }
    
    public static Set<Class<?>> scanPackage(String... packages){
        return scanPackage(null, packages);
    }
    
    public static Set<Class<?>> scanPackage(ClassFilter filter, String... packages){
        Set<Class<?>> classes = new LinkedHashSet<>();
        Resource[] resources = getResources(packages);
        for(Resource resource : resources){
            Class<?> clazz = resolveClassFromResource(resource);
            if(filter == null || filter.access(clazz)){
                classes.add(clazz);
            }
        }
        return classes;
    }
    
    private static Resource[] getResources(String... packages){
        try{
            if(packages != null){
                Set<Resource> resources = new LinkedHashSet<>();
                for(String pac : packages){
                    if(pac == null){
                        pac = "";
                    }else{
                        pac = pac.replaceAll(LL_DOT, "/");
                        if(!pac.endsWith("/")){
                            pac += "/";
                        }
                    }
                    pac = "classpath*:" + pac + "**/*.class";
                    resources.addAll(Arrays.asList(resourceLoader.getResources(pac)));
                    return resources.toArray(new Resource[0]);
                }
            }
            return resourceLoader.getResources("classpath*:**/*.class");
        }catch(IOException e){
            logger.error("scan packages error", e);
            throw new RuntimeException("scan packages error", e);
        }
    }
    
    private static Class<?> resolveClassFromResource(Resource resource){
        try{
            InputStream in = new BufferedInputStream(resource.getInputStream());
            ClassReader classReader = new ClassReader(in);
            String className = classReader.getClassName().replaceAll("/", DOT);
            return ClassUtils.forName(className, ClassUtils.getDefaultClassLoader());
        }catch(IOException e){
            logger.error("ASM ClassReader failed to parse class file - probably due to a new java class version that is not supported yet: " + resource, e);
            throw new RuntimeException("ASM ClassReader failed to parse class file - probably due to a new java class version that is not supported yet: " + resource, e);
        } catch (ClassNotFoundException | LinkageError e) {
            logger.error("Load class error", e);
            throw new RuntimeException("Load class error", e);
        }
    }
    
    /**
     * Java application load
     */
    public static Set<Class<?>> getClasses(String name, boolean isRecursive) {
        name = null == name ? "" : name;
        Set<Class<?>> classes = new LinkedHashSet<>();
        String packageName = name.replaceAll(LL_DOT, "/");
        
        try {
            Enumeration<URL> dirs = ClassLoader.getSystemClassLoader().getResources(packageName);
            while(dirs.hasMoreElements()){
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                if("file".equals(protocol)){
                    logger.debug("-----------file类型的扫描-----------");
                    // obtain physics path
                    String filePath = URLDecoder.decode(url.getFile(), System.getProperty("file.encoding"));
                    addClassesByFile(classes, name, filePath, isRecursive);
                }
            }
        } catch (IOException e) {
            logger.error("Cann't scan package: " + name, e);
        }
        return classes;
    }
    
    private static void addClassesByFile(Set<Class<?>> classes,
                                         String packageName,
                                         String filePath,
                                         final boolean isRecursive){
        File dir = new File(filePath);
        if(!dir.exists() || !dir.isDirectory()){
            logger.error("Package not exist: " + packageName);
            return;
        }
        File[] files = dir.listFiles(new FileFilter(){

            @Override
            public boolean accept(File file){
                return isRecursive && file.isDirectory() || file.getName().endsWith(".class");
            }
            
        });
        
        for(File file : files){
            if(file.isDirectory()){
                StringBuffer sb = new StringBuffer(packageName);
                if(sb.length() > 0){
                    sb.append(DOT);
                }
                sb.append(file.getName());
                addClassesByFile(classes, sb.toString(), file.getAbsolutePath(), isRecursive);
            }else{
                String className = file.getName().substring(0, file.getName().length() - 6);
                try{
                    StringBuffer sb = new StringBuffer(packageName);
                    if(sb.length() > 0){
                        sb.append(DOT);
                    }
                    sb.append(className);
                    Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(sb.toString());
                    classes.add(clazz);
                }catch(ClassNotFoundException e){
                    StringBuffer msg = new StringBuffer(packageName);
                    if(msg.length() > 0){
                        msg.append(DOT);
                    }
                    msg.append(className).append("not found");
                    logger.error(msg.toString(), e);
                }
            }
        }
    }
    
    @SuppressWarnings("unused")
    private void addClassesByJar(){
        
    }
    
    @SuppressWarnings("unused")
    private static String[] getClassPathArray() {
        //不包括 jre
        return System.getProperty("java.class.path").split(System.getProperty("path.separator"));
        
        /* 包括 jre 
        return System.getProperty("java.class.path").
               concat(System.getProperty("path.separator")).
               concat(System.getProperty("java.home")).
               split(System.getProperty("path.separator"));*/
    }
    
}
