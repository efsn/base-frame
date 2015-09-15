package com.codeyn.sitemesh.rule;

import java.io.IOException;

import org.sitemesh.content.ContentProperty;
import org.sitemesh.tagprocessor.BasicBlockRule;
import org.sitemesh.tagprocessor.Tag;

public class SiteMeshTemplateTagRule extends BasicBlockRule<String> {

    private final ContentProperty propertyToExport;

    public SiteMeshTemplateTagRule(ContentProperty propertyToExport) {
        this.propertyToExport = propertyToExport;
    }

    @Override
    protected String processStart(Tag tag) throws IOException {
        if (shouldCapture(tag)) {
            pushContent();
        }
        return getPath(tag);
    }

    @Override
    protected void processEnd(Tag tag, String path) throws IOException {
        if (path != null) {
            CharSequence tagContent = popContent();
            propertyToExport.setValue(path);
            ensureContentIsNotConsumed(tagContent);
        }
    }

    private void ensureContentIsNotConsumed(CharSequence content) throws IOException {
        tagProcessorContext.currentBuffer().append(content);
    }

    private CharSequence popContent() {
        CharSequence content = tagProcessorContext.currentBufferContents();
        tagProcessorContext.popBuffer();
        return content;
    }

    private void pushContent() {
        tagProcessorContext.pushBuffer();
    }

    private String getPath(Tag tag) {
        return tag.getAttributeValue("path", false);
    }

    private boolean shouldCapture(Tag tag) {
        return tag.hasAttribute("path", false);
    }

}
