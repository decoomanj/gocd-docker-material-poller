/*
The MIT License (MIT)

Copyright (c) 2014 Jan De Cooman

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package com.braindrainpain.docker;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.RepositoryConfiguration;

import java.io.IOException;
import java.text.MessageFormat;
import com.thoughtworks.go.plugin.api.logging.Logger;
import java.util.Map;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * Docker Repository connector.
 *
 * @author Jan De Cooman
 */
public class DockerRepository extends HttpSupport {

    final private static Logger LOG = Logger.getLoggerFor(DockerRepository.class);

    final private RepositoryConfiguration repositoryConfiguration;

    private DockerRepository(RepositoryConfiguration repositoryConfiguration) {
        this.repositoryConfiguration = repositoryConfiguration;
    }

    public static DockerRepository getInstance(RepositoryConfiguration repositoryConfiguration) {
        return new DockerRepository(repositoryConfiguration);
    }

    public DockerTag getLatestRevision(final PackageConfiguration packageConfiguration) {
        String tagName = packageConfiguration.get(Constants.TAG).getValue();        
        JsonObject jsonTags = this.allTags(packageConfiguration);
        return this.getLatestTag(jsonTags, tagName);
    }

    private DockerTag getLatestTag(final JsonObject tags, final String tagName) {
        DockerTag result = null;
        for (Map.Entry<String, JsonElement> entry : tags.entrySet()) {
            if (tagName.equals(entry.getKey())) {
                result = new DockerTag(entry.getKey(), entry.getValue().getAsString());
                LOG.info("Found tag: " + result);
                break;
            }
        }
        return result;
    }

    /**
     * Call the Docker API.
     * 
     * @param packageConfiguration
     * @return 
     */
    private JsonObject allTags(final PackageConfiguration packageConfiguration) {
        JsonObject result = null;
        HttpClient client = getHttpClient();

        String repository = MessageFormat.format("{0}/v1/repositories/{1}/tags",
                repositoryConfiguration.get(Constants.REGISTRY).getValue(),
                packageConfiguration.get(Constants.REPOSITORY).getValue());
        
        try {
            GetMethod get = new GetMethod(repository);
            if (client.executeMethod(get) == HttpStatus.SC_OK) {
                String jsonString = get.getResponseBodyAsString();
                LOG.info("RECIEVED: " + jsonString);
                result = (JsonObject) new JsonParser().parse(jsonString);
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot fetch the tags from " + repository, e);
        }

        return result;
    }

}
