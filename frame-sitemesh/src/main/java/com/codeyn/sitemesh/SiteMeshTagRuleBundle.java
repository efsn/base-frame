package com.codeyn.sitemesh;

import org.sitemesh.SiteMeshContext;
import org.sitemesh.content.ContentProperty;
import org.sitemesh.content.tagrules.TagRuleBundle;
import org.sitemesh.tagprocessor.State;

import com.codeyn.sitemesh.rule.SiteMeshAttrTagRule;
import com.codeyn.sitemesh.rule.SiteMeshComponentTagRule;
import com.codeyn.sitemesh.rule.SiteMeshTemplateTagRule;
import com.codeyn.sitemesh.rule.SiteMeshInsertTagRule;

/**
 * 
 * @author Arthur
 *
 */
public class SiteMeshTagRuleBundle implements TagRuleBundle {

    public void install(State defaultState, ContentProperty contentProperty, SiteMeshContext siteMeshContext) {
        defaultState.addRule("sm:insert", new SiteMeshInsertTagRule(siteMeshContext));
        defaultState.addRule("sm:template", new SiteMeshTemplateTagRule(contentProperty.getChild(Const.TEMPLATE_PATH)));
        defaultState.addRule("sm:component", new SiteMeshComponentTagRule(contentProperty.getChild(Const.COMPONENTS)));
        defaultState.addRule("sm:attr", new SiteMeshAttrTagRule(siteMeshContext));
    }

    public void cleanUp(State defaultState, ContentProperty contentProperty, SiteMeshContext siteMeshContext) {
        // Nothing.
    }

}
