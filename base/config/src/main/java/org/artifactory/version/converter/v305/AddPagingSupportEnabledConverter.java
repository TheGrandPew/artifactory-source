package org.artifactory.version.converter.v305;

import org.artifactory.common.ConstantValues;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Igor Usenko
 */
public class AddPagingSupportEnabledConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(AddPagingSupportEnabledConverter.class);

    @Override
    public void convert(Document doc) {
        String simpleName = this.getClass().getSimpleName();
        log.info("Started {}", simpleName);
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        Element securityElement = rootElement.getChild("security", namespace);
        if (securityElement != null) {
            Element ldapSettingsElement = securityElement.getChild("ldapSettings", namespace);
            if (ldapSettingsElement != null) {
                List<Element> ldapSettingElements = ldapSettingsElement.getChildren();
                ldapSettingElements.forEach(e -> {
                    List<Element> pagingSupportEnabledElements = e.getChildren("pagingSupportEnabled",
                            namespace);
                    if (pagingSupportEnabledElements.isEmpty()) {
                        Element pagingSupportEnabled = new Element("pagingSupportEnabled", namespace);
                        String pagingSupport = String.valueOf(ConstantValues.ldapPagingSupport.getBoolean());
                        pagingSupportEnabled.addContent(pagingSupport);
                        e.addContent(pagingSupportEnabled);
                    }
                });
            }
        }
        log.info("Finished {}", simpleName);
    }

}