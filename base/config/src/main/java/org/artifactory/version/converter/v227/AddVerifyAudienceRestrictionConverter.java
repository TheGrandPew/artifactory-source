package org.artifactory.version.converter.v227;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds 'verifyAudienceRestriction' and convert value to 'false' only in case samlSettings exists,
 * and 'verify audience restriction is not exist. missing samlSettings represent a new installation, and default
 * value in this case is 'true'.
 *
 * @author Aviv Anidjar
 */
public class AddVerifyAudienceRestrictionConverter implements XmlConverter {

    private static final Logger log = LoggerFactory.getLogger(AddVerifyAudienceRestrictionConverter.class);
    private static final String VERIFY_AUDIENCE_RESTRICTION = "verifyAudienceRestriction";

    @Override
    public void convert(Document doc) {
        String simpleName = this.getClass().getSimpleName();
        log.info("Started {}", simpleName);
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        Element securityElement = rootElement.getChild("security", namespace);
        if (securityElement != null) {
            Element samlSettingsElement = securityElement.getChild("samlSettings", namespace);
            if (samlSettingsElement != null) {
                Element verifyAudienceRestrictionElement = samlSettingsElement
                        .getChild(VERIFY_AUDIENCE_RESTRICTION, namespace);
                if (verifyAudienceRestrictionElement == null) {
                    Element enableIntegration = samlSettingsElement.getChild("enableIntegration", namespace);
                    int addAt = samlSettingsElement.indexOf(enableIntegration) + 1;
                    Element newVerifyAudienceRestrictionElement = new Element(VERIFY_AUDIENCE_RESTRICTION, namespace);
                    newVerifyAudienceRestrictionElement.setText("false");
                    samlSettingsElement.addContent(addAt, newVerifyAudienceRestrictionElement);
                }
            }
            log.info("Finished {}", simpleName);
        }
    }
}
