package com.codeyn.sitemesh.rule;

import java.io.IOException;

import org.sitemesh.content.ContentProperty;
import org.sitemesh.tagprocessor.BasicBlockRule;
import org.sitemesh.tagprocessor.Tag;

public class SiteMeshComponentTagRule extends BasicBlockRule<String> {

    private final ContentProperty propertyToExport;

    public SiteMeshComponentTagRule(ContentProperty propertyToExport) {
        this.propertyToExport = propertyToExport;
    }

    @Override
    protected String processStart(Tag tag) throws IOException {
        if (shouldCapture(tag)) {
            pushContent();
        }
        return getName(tag);
    }

    @Override
    protected void processEnd(Tag tag, String name) throws IOException {
        if (capturing(name)) {
            CharSequence tagContent = popContent();
            propertyToExport.getChild(name).setValue(tagContent);
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

    private boolean capturing(String name) {
        return name != null;
    }

    private void pushContent() {
        tagProcessorContext.pushBuffer();
    }

    private String getName(Tag tag) {
        return tag.getAttributeValue("name", false);
    }

    private boolean shouldCapture(Tag tag) {
        return tag.hasAttribute("name", false);
    }

}
