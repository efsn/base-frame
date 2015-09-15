package com.codeyn.sitemesh.rule;

import java.io.IOException;

import org.sitemesh.SiteMeshContext;
import org.sitemesh.tagprocessor.BasicRule;
import org.sitemesh.tagprocessor.Tag;
import org.sitemesh.webapp.WebAppContext;

public class SiteMeshAttrTagRule extends BasicRule {

    private final WebAppContext webAppContext;

    public SiteMeshAttrTagRule(SiteMeshContext siteMeshContext) {
        this.webAppContext = (WebAppContext) siteMeshContext;
    }

    @Override
    public void process(Tag tag) throws IOException {
        String name = tag.getAttributeValue("name", false);
        String value = tag.getAttributeValue("value", false);
        if (name != null && !"".equals(name.trim()) && value != null) {
            webAppContext.getRequest().setAttribute(name, value);
        }
    }

}
