package org.artifactory.properties;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.artifactory.md.Properties;
import org.artifactory.model.xstream.fs.PropertiesImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

/**
 * @author dudim
 */
public class PropertiesServiceImplTest {

    @Test
    public void testSetPropertiesWithInternalPropertiesNotInvokeInterceptors() {
        PropertiesServiceImpl propertiesServiceSpy = spy(new PropertiesServiceImpl());

        Mockito.doReturn(true).when(propertiesServiceSpy).setMetadataProperties(any(),any());
        RepoPath repoPath = RepoPathFactory.create("repo-example", "");
        Properties properties = new PropertiesImpl();
        propertiesServiceSpy.setProperties(repoPath, properties, true);

        verify(propertiesServiceSpy, times(1)).setMetadataProperties(any(), any());
        verify(propertiesServiceSpy, times(0)).setArtifactProperties(any(), any());
        verify(propertiesServiceSpy, times(0)).goThroughBeforeCreateProperties(any(), any(), any(), any());
        verify(propertiesServiceSpy, times(0)).goThroughAfterCreateProperties(any(), any(), any());
        verify(propertiesServiceSpy, times(0)).goThroughBeforeDeleteProperties(any(), any(), any(), any());
        verify(propertiesServiceSpy, times(0)).goThroughAfterDeleteProperties(any(), any(), any());
    }

    @DataProvider
    public static Object[][] properties() {
        return new Object[][] {

                {
                        a(p("disttag", "latest")), // current
                        a(p("disttag", "")), // modified
                        a(p("disttag", "")), // expected modified
                        a() // expected deleted
                },
                {
                        a(p("disttag", "latest")), // current
                        a(p("disttag", "latest")), // modified
                        a(), // expected modified
                        a() // expected deleted
                },

                {
                        a(p("disttag", "latest")), // current
                        a(p("disttag", null)), // modified
                        a(p("disttag", null)), // expected modified
                        a() // expected deleted
                },
                {
                        a(p("disttag", null)), // current
                        a(p("disttag", "latest")), // modified
                        a(p("disttag", "latest")), // expected modified
                        a() // expected deleted
                },
                {
                        a(), // current
                        a(p("disttag", "latest")), // modified
                        a(p("disttag", "latest")), // expected modified
                        a() // expected deleted
                },
                {
                        a(p("disttag", "latest")), // current
                        a(), // modified
                        a(), // expected modified
                        a(p("disttag", "")) // expected deleted
                },

                {
                        a(p("disttag", "latest")), // current
                        a(p("disttag", "latest"), p("disttag", "release")), // modified
                        a(p("disttag", "latest"), p("disttag", "release")), // expected modified
                        a() // expected deleted
                },
                {
                        a(p("disttag", "latest"), p("disttag", "release")), // current
                        a(p("disttag", "latest")), // modified
                        a(p("disttag", "latest")), // expected modified
                        a() // expected deleted
                },
                {
                        a(p("disttag", "latest"), p("disttag", "release")),  // current
                        a(), // modified
                        a(), // expected modified
                        a(p("disttag", "")) // expected deleted
                }
        };
    }

    @Test(dataProvider = "properties")
    public void testFilterPreExistingProperties(
            Pair<String, String>[] currentPairs,
            Pair<String, String>[] modifiedPairs,
            Pair<String, String>[] expectedModifiedPairs,
            Pair<String, String>[] expectedDeletedPairs
    ) {
        Multimap<String, String> current = toMultimap(currentPairs);
        Properties currentProperties = new PropertiesImpl();
        currentProperties.putAll(current);

        Multimap<String, String> modified = toMultimap(modifiedPairs);
        Properties modifiedProperties = new PropertiesImpl();
        modifiedProperties.putAll(modified);

        Multimap<String, String> expectedModified = toMultimap(expectedModifiedPairs);
        Properties expectedModifiedProperties = new PropertiesImpl();
        expectedModifiedProperties.putAll(expectedModified);

        Multimap<String, String> expectedDeleted = toMultimap(expectedDeletedPairs);
        Properties expectedDeletedProperties = new PropertiesImpl();
        expectedDeletedProperties.putAll(expectedDeleted);

        PropertiesServiceImpl.PropertiesDiff properties =
                new PropertiesServiceImpl().calcPropertiesDiff(modifiedProperties, currentProperties);

        assertEquals(properties.getModified(), expectedModifiedProperties);
        assertEquals(properties.getDeleted(), expectedDeletedProperties);
    }

    private static Pair<String, String> p(String first, String second) {
        return new ImmutablePair<>(first, second);
    }

    private static Pair[] a(Pair... pairs) {
        return pairs;
    }

    private static Multimap<String, String> toMultimap(Pair<String, String>[] pairs) {
        return Arrays.stream(pairs).
                collect(
                        ArrayListMultimap::create,
                        (m, p) -> m.put(p.getKey(), p.getValue()),
                        Multimap::putAll
                );
    }

}