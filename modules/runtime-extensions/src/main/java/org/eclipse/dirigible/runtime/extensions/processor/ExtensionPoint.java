package org.eclipse.dirigible.runtime.extensions.processor;

import java.util.List;

import org.eclipse.dirigible.core.extensions.definition.ExtensionDefinition;
import org.eclipse.dirigible.core.extensions.definition.ExtensionPointDefinition;

public class ExtensionPoint {
	
	private ExtensionPointDefinition extensionPoint;
	
	private List<ExtensionDefinition> extensions;

	public ExtensionPoint(ExtensionPointDefinition extensionPoint, List<ExtensionDefinition> extensions) {
		super();
		this.extensionPoint = extensionPoint;
		this.extensions = extensions;
	}
	
	public ExtensionPointDefinition getExtensionPoint() {
		return extensionPoint;
	}
	
	public List<ExtensionDefinition> getExtensions() {
		return extensions;
	}
	
}