/**
 * Copyright (C) 2013, 2014 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.converter.adapt;

public class JsonModelValidationException extends Exception {

	private static final long serialVersionUID = -7802345789736329818L;

	public JsonModelValidationException() {
		super();
	}

	public JsonModelValidationException(final Throwable throwable) {
		super(throwable);
	}

	public JsonModelValidationException(final String message, final Throwable t) {
		super(message, t);
	}

}
