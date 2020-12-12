package org.artifactory.version.converter.v227;

import org.artifactory.version.converter.v160.AddonsLayoutConverterTestBase;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@Test
public class AddVerifyAudienceRestrictionToSamlSettingsConverterTest extends AddonsLayoutConverterTestBase {

    @Test
    public void addVerifyAudienceRestrictionToSamlSettingsConverterTest() throws Exception {
        AddVerifyAudienceRestrictionConverter converter = new AddVerifyAudienceRestrictionConverter();
        String CONFIG_XML = "/config/test/config.2.2.5_with_saml_settings_enabled.xml";
        Document document = convertXml(CONFIG_XML, converter);
        Element root = document.getRootElement();
        Namespace ns = root.getNamespace();
        Element security = root.getChild("security", ns);
        Element samlSettings = security.getChild("samlSettings", ns);
        assertEquals(samlSettings.getChild("verifyAudienceRestriction", ns).getValue(), "false");
    }

    @Test
    public void dontAddVerifyAudienceRestrictionToSamlSettingsConverterTest() throws Exception {
        AddVerifyAudienceRestrictionConverter converter = new AddVerifyAudienceRestrictionConverter();
        String CONFIG_XML = "/config/test/config.2.2.5_with_saml_settings_and_verify_audience_restriction.xml";
        Document document = convertXml(CONFIG_XML, converter);
        Element root = document.getRootElement();
        Namespace ns = root.getNamespace();
        Element security = root.getChild("security", ns);
        Element samlSettings = security.getChild("samlSettings", ns);
        // added new field with value false when samlSettings exists.
        assertEquals(samlSettings.getChild("verifyAudienceRestriction", ns).getValue(), "true");
    }
}
