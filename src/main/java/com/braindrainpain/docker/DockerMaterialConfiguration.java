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

import com.thoughtworks.go.plugin.api.config.Configuration;
import com.thoughtworks.go.plugin.api.config.Property;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialProperty;
import com.thoughtworks.go.plugin.api.material.packagerepository.RepositoryConfiguration;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import static com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialProperty.*;
import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 * Docker Material Configuration.
 *
 * @author Jan De Cooman
 */
public class DockerMaterialConfiguration implements PackageMaterialConfiguration {

    final private static Logger LOG = Logger.getLoggerFor(DockerMaterialConfiguration.class);

    @Override
    public RepositoryConfiguration getRepositoryConfiguration() {
        RepositoryConfiguration repositoryConfiguration = new RepositoryConfiguration();
        repositoryConfiguration.add(new PackageMaterialProperty(Constants.REGISTRY).
                with(DISPLAY_NAME, "Registry URL").with(DISPLAY_ORDER, 0));
        return repositoryConfiguration;
    }

    @Override
    public PackageConfiguration getPackageConfiguration() {
        PackageConfiguration packageConfiguration = new PackageConfiguration();
        packageConfiguration.add(new PackageMaterialProperty(Constants.REPOSITORY).
                with(DISPLAY_NAME, "Repository").with(DISPLAY_ORDER, 0));
        packageConfiguration.add(new PackageMaterialProperty(Constants.TAG, "latest").
                with(REQUIRED, false).with(DISPLAY_NAME, "Tag").with(DISPLAY_ORDER, 1));
        return packageConfiguration;
    }

    /**
     * Check the validity of the registry URL
     *
     * @param repositoryConfiguration
     * @return
     */
    @Override
    public ValidationResult isRepositoryConfigurationValid(final RepositoryConfiguration repositoryConfiguration) {
        LOG.info("Validating repository: " + repositoryConfiguration.get(Constants.REGISTRY).getValue());
        ValidationResult validationResult = new ValidationResult();
        this.validateKeys(getRepositoryConfiguration(), repositoryConfiguration, validationResult);

        Property registry = repositoryConfiguration.get(Constants.REGISTRY);
        if (registry == null) {
            validationResult.addError(new ValidationError(Constants.REGISTRY, "Registry url not specified"));
            return validationResult;
        }

        DockerRegistry.getInstance(registry.getValue()).validate(validationResult);
        return validationResult;
    }

    /**
     * Check if the package specific fields are set.
     *
     * @param packageConfiguration
     * @param repositoryConfiguration
     * @return
     */
    @Override
    public ValidationResult isPackageConfigurationValid(PackageConfiguration packageConfiguration, RepositoryConfiguration repositoryConfiguration) {
        ValidationResult validationResult = new ValidationResult();
        this.validateKeys(getPackageConfiguration(), packageConfiguration, validationResult);

        Property repository = packageConfiguration.get(Constants.REPOSITORY);
        if (repository == null) {
            validationResult.addError(new ValidationError(Constants.REPOSITORY, "Repository not specified"));
            return validationResult;
        }
        String repositoryName = repository.getValue();
        if (StringUtils.isEmpty(repositoryName)) {
            validationResult.addError(new ValidationError(Constants.REPOSITORY, "Repository is empty or not set"));
            return validationResult;
        }

        Property tag = packageConfiguration.get(Constants.TAG);
        if (tag == null) {
            validationResult.addError(new ValidationError(Constants.TAG, "Tag not specified"));
            return validationResult;
        }
        String tagName = tag.getValue();
        if (StringUtils.isEmpty(tagName)) {
            validationResult.addError(new ValidationError(Constants.TAG, "Tag is empty or not set"));
            return validationResult;
        }

        return validationResult;
    }

    /**
     * Validate the fields of both configuration.
     *
     * @param packageConfiguration
     * @param repositoryConfiguration
     * @param validationResult
     */
    public void validate(
            final PackageConfiguration packageConfiguration,
            final RepositoryConfiguration repositoryConfiguration,
            final ValidationResult validationResult) {

        ValidationResult repositoryConfigurationValidationResult = isRepositoryConfigurationValid(repositoryConfiguration);
        validationResult.addErrors(repositoryConfigurationValidationResult.getErrors());
        ValidationResult packageConfigurationValidationResult = isPackageConfigurationValid(packageConfiguration, repositoryConfiguration);
        validationResult.addErrors(packageConfigurationValidationResult.getErrors());
    }

    /**
     * Filter out unwanted keys
     */
    private void validateKeys(Configuration configDefinedByPlugin, Configuration configDefinedByUser, ValidationResult validationResult) {
        List<String> validKeys = new ArrayList<>();
        List<String> invalidKeys = new ArrayList<>();
        for (Property configuration : configDefinedByPlugin.list()) {
            validKeys.add(configuration.getKey());
        }

        for (Property configuration : configDefinedByUser.list()) {
            if (!validKeys.contains(configuration.getKey())) {
                invalidKeys.add(configuration.getKey());
            }
        }
        if (!invalidKeys.isEmpty()) {
            validationResult.addError(new ValidationError("", String.format("Unsupported key(s) found : %s. Allowed key(s) are : %s", join(invalidKeys), join(validKeys))));
        }
    }

    private String join(final List<String> keys) {
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            sb.append(" ").append(key);
        }
        return sb.toString();
    }
}
