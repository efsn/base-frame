package com.codeyn.sitemesh.rule;

import java.io.IOException;

import org.sitemesh.SiteMeshContext;
import org.sitemesh.content.Content;
import org.sitemesh.content.ContentProperty;
import org.sitemesh.content.tagrules.decorate.SiteMeshWriteRule;
import org.sitemesh.tagprocessor.Tag;

import com.codeyn.sitemesh.Const;

public class SiteMeshInsertTagRule extends SiteMeshWriteRule {

    protected SiteMeshContext siteMeshContext;

    public SiteMeshInsertTagRule(SiteMeshContext siteMeshContext) {
        super(siteMeshContext);
        this.siteMeshContext = siteMeshContext;
    }

    @Override
    protected Object processStart(Tag tag) throws IOException {
        super.processStart(tag);
        return tag.getAttributeValue("property", true);
    }

    @Override
    protected ContentProperty getProperty(Content content, String propertyPath) {
        propertyPath = Const.COMPONENTS + "." + propertyPath;
        return super.getProperty(content, propertyPath);
    }

    @Override
    protected void processEnd(Tag tag, Object propertyPath) throws IOException {
        CharSequence defaultContents = tagProcessorContext.currentBufferContents();
        tagProcessorContext.popBuffer();
        if (siteMeshContext.getContentToMerge() == null) {
            tagProcessorContext.currentBuffer().append(defaultContents);
        } else if (propertyPath != null) {
            // 如果要插入的节点不存在，则用默认值代替
            String text = getProperty(siteMeshContext.getContentToMerge(), (String) propertyPath).getValue();
            if (text == null) {
                tagProcessorContext.currentBuffer().append(defaultContents);
            }
        }
    }

}
