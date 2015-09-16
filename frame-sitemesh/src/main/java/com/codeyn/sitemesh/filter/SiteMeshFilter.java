package com.codeyn.sitemesh.filter;

import javax.servlet.Filter;
import javax.servlet.ServletException;

import org.sitemesh.builder.SiteMeshFilterBuilder;
import org.sitemesh.config.ConfigurableSiteMeshFilter;

import com.codeyn.sitemesh.SiteMeshTagRuleBundle;

/**
 * <p>page decorator, if needed just add filter config in web.xml</p>
 * <code>
 * 
     <p>&lt;filter&gt;
     <p>&lt;filter-name&gt;sitemesh&lt;/filter-name&gt;
     <p>&lt;filter-class&gt;com.codeyn.sitemesh.filter.SiteMeshFilter&lt;/filter-class&gt;
     <p>&lt;/filter&gt;
     <p>&lt;filter-mapping&gt;
     <p>&lt;filter-name&gt;sitemesh&lt;/filter-name&gt;
     <p>&lt;url-pattern&gt;/*&lt;/url-pattern&gt;
     <p>&lt;/filter-mapping&gt;
  
 * </code>
 * 
 * 
 * 
 * @author Arthur
 *
 */
public class SiteMeshFilter extends ConfigurableSiteMeshFilter {

    private BaseSiteMeshFilter baseFilter;

    @Override
    protected void applyCustomConfiguration(SiteMeshFilterBuilder builder) {
        builder.setTagRuleBundles(new SiteMeshTagRuleBundle());
        baseFilter = new BaseSiteMeshFilter(builder.getSelector(), builder.getContentProcessor());
    }

    @Override
    protected Filter setup() throws ServletException {
        super.setup();
        return baseFilter;
    }

}
