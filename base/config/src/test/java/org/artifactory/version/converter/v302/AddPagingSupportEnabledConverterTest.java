package org.artifactory.version.converter.v302;

import org.artifactory.common.ConstantValues;
import org.artifactory.version.converter.v160.AddonsLayoutConverterTestBase;
import org.artifactory.version.converter.v305.AddPagingSupportEnabledConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Igor Usenko
 */
@Test
public class AddPagingSupportEnabledConverterTest extends AddonsLayoutConverterTestBase {

    @Test
    public void whenNoLdapSettingsThenNothingChangedForBothLdapPagingSupportSettingValue() throws Exception {
        whenNoLdapSettingsThenNothingChanged(false);
        whenNoLdapSettingsThenNothingChanged(true);
    }

    @Test
    public void whenLdapSettingExistsThenItIsChangedToLdapPagingSupportSettingValue() throws Exception {
        whenLdapSettingExistsThenItIsChanged(false);
        whenLdapSettingExistsThenItIsChanged(true);
    }

    private void whenNoLdapSettingsThenNothingChanged(boolean ldapPagingSupport) throws Exception {
        homeStub.setProperty(ConstantValues.ldapPagingSupport, Boolean.toString(ldapPagingSupport));

        Document document = convertXml("/config/test/config.2.2.5_without_ldap_settings.xml",
                new AddPagingSupportEnabledConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        Element securityElement = rootElement.getChild("security", namespace);
        Element ldapSettingsElement = securityElement.getChild("ldapSettings", namespace);
        List<Element> ldapSettingElements = ldapSettingsElement.getChildren();

        assertTrue(ldapSettingElements.isEmpty());
    }

    private void whenLdapSettingExistsThenItIsChanged(boolean ldapPagingSupport) throws Exception {
        homeStub.setProperty(ConstantValues.ldapPagingSupport, Boolean.toString(ldapPagingSupport));

        Document document = convertXml("/config/test/config.2.2.5_with_ldap_settings.xml",
                new AddPagingSupportEnabledConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        Element securityElement = rootElement.getChild("security", namespace);
        Element ldapSettingsElement = securityElement.getChild("ldapSettings", namespace);
        List<Element> ldapSettingElements = ldapSettingsElement.getChildren();

        assertEquals(1, ldapSettingElements.size());

        Element ldapSettingElement = ldapSettingElements.get(0);
        Element pagingSupportEnabledElement = ldapSettingElement.getChild("pagingSupportEnabled", namespace);
        String value = pagingSupportEnabledElement.getContent(0).getValue();

        assertEquals(ldapPagingSupport, Boolean.parseBoolean(value));
    }

}