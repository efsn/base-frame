package com.codeyn.sitemesh.filter;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sitemesh.content.Content;
import org.sitemesh.content.ContentProcessor;
import org.sitemesh.content.ContentProperty;
import org.sitemesh.webapp.WebAppContext;
import org.sitemesh.webapp.contentfilter.ContentBufferingFilter;
import org.sitemesh.webapp.contentfilter.ResponseMetaData;
import org.sitemesh.webapp.contentfilter.Selector;

import com.codeyn.sitemesh.Const;

public class BaseSiteMeshFilter extends ContentBufferingFilter {

    private final ContentProcessor contentProcessor;

    public BaseSiteMeshFilter(Selector selector, ContentProcessor contentProcessor) {
        super(selector);
        this.contentProcessor = contentProcessor;
    }

    @Override
    protected boolean postProcess(String contentType,
                                  CharBuffer buffer,
                                  HttpServletRequest request,
                                  HttpServletResponse response,
                                  ResponseMetaData metaData) throws IOException, ServletException {
        WebAppContext context = createContext(contentType, request, response, metaData);
        Content content = contentProcessor.build(buffer, context);
        if (content == null) {
            return false;
        }
        List<String> decoratedPaths = new ArrayList<String>();
        String decoratorPath = decoratorSelect(content);
        
        // 防止出现循环装饰
        while (decoratorPath != null && !isDecorated(decoratedPaths, decoratorPath)) {
            content = context.decorate(decoratorPath, content);
            decoratedPaths.add(decoratorPath);
            decoratorPath = decoratorSelect(content);
        }

        if (content == null) {
            return false;
        }
        content.getData().writeValueTo(response.getWriter());
        return true;
    }

    protected String decoratorSelect(Content content) {
        ContentProperty properties = content.getExtractedProperties().getChild(Const.TEMPLATE_PATH);
        return properties.getValue();
    }

    private boolean isDecorated(List<String> decoratedPaths, String decoratorPath) {
        for (String path : decoratedPaths) {
            if (decoratorPath.equalsIgnoreCase(path)) {
                return true;
            }
        }
        return false;
    }

    protected WebAppContext createContext(String contentType,
                                          HttpServletRequest request,
                                          HttpServletResponse response,
                                          ResponseMetaData metaData) {
        return new WebAppContext(contentType, request, response,
                                 getFilterConfig().getServletContext(),
                                 contentProcessor, metaData, true);
    }

}
