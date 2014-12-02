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

import com.thoughtworks.go.plugin.api.config.Property;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.material.packagerepository.RepositoryConfiguration;
import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;

/**
 * Docker Registry connector.
 *
 * @author Jan De Cooman
 */
public class DockerRegistry extends HttpSupport {

    final private Logger LOG = Logger.getLoggerFor(DockerRegistry.class);

    final private String url;

    final private static List<String> protocols = new ArrayList<>(2);

    /**
     * Supported protocols.
     */
    static {
        protocols.add("http");
        protocols.add("https");
    }

    /**
     * Create a new instance of the DockerRegistry
     * 
     * @param url RegistryURL
     */
    private DockerRegistry(final String url) {
        this.url = url;
    }

    public static DockerRegistry getInstance(final String url) {
        return new DockerRegistry(url);
    }
    
    public static DockerRegistry getInstance(
            final RepositoryConfiguration repositoryPluginConfigurations) {
        
        Property registry = repositoryPluginConfigurations.get(Constants.REGISTRY);
        return new DockerRegistry(registry.getValue());
    }
    
    /**
     * Validate the URL.
     *
     * @param validationResult The list with invalid fields.
     */
    public void validate(final ValidationResult validationResult) {
        try {
            if (StringUtils.isBlank(url)) {
                validationResult.addError(new ValidationError(Constants.REGISTRY, "URL is empty"));
                return;
            }
            URL validatedUrl = new URL(this.url);
            if (!protocols.contains(validatedUrl.getProtocol())) {
                validationResult.addError(new ValidationError(Constants.REGISTRY, "Invalid URL: Only 'http' and 'https' protocols are supported."));
            }

            if (StringUtils.isNotBlank(validatedUrl.getUserInfo())) {
                validationResult.addError(new ValidationError(Constants.REGISTRY, "User info should not be provided as part of the URL. Please provide credentials using USERNAME and PASSWORD configuration keys."));
            }
        } catch (MalformedURLException e) {
            validationResult.addError(new ValidationError(Constants.REGISTRY, "Invalid URL : " + url));
        }
    }

    /**
     * Checks the connection to the registry
     */
    public void checkConnection() {
        LOG.debug("Checking: '" + url + "'");
        HttpClient client = getHttpClient();
        GetMethod method = new GetMethod(url);
        method.setFollowRedirects(false);
        try {
            int returnCode = client.executeMethod(method);
            if (returnCode != HttpStatus.SC_OK) {
                LOG.error("Not ok from: '" + url + "'");
                throw new RuntimeException("Not ok from: '" + url +"'");
            }
        } catch (IOException e) {
            LOG.error("Error connecting to: '" + url + "'");
            throw new RuntimeException("Error connecting to: '" + url +"'");
        }
    }

    public String getUrl() {
        return url;
    }
    
    
}
