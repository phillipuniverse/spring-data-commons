/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.mapping.model;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mapping.Person;
import org.springframework.data.mapping.PersonTypeInformation;
import org.springframework.data.mapping.PersonWithId;
import org.springframework.data.mapping.PersonWithIdTypeInformation;
import org.springframework.data.util.TypeInformation;

/**
 * @author Christoph Strobl
 * @since 2020/10
 */
class ConfigurableTypeInformationUnitTests {

	@Test // DATACMNS-???
	void domainTypeInformationSatisifesInterface() {

		ConfigurableTypeInformation<Person> typeInformation = new ConfigurableTypeInformation(Person.class);

		assertThat(typeInformation.getActualType()).isSameAs(typeInformation);
		assertThat(typeInformation.getAnnotations()).isEmpty();
	}

	@Test // DATACMNS-???
	void domainTypeInformationContainsOwnFields() {

		ConfigurableTypeInformation<Person> typeInformation = PersonTypeInformation.instance();

		assertThat(typeInformation.getProperty("firstName")).isNotNull();
		assertThat(typeInformation.getProperty("id")).isNull();
	}

	@Test // DATACMNS-???
	void domainTypeInformationContainsFieldsOfParentType() {

		ConfigurableTypeInformation<PersonWithId> typeInformation = PersonWithIdTypeInformation.instance();

		assertThat(typeInformation.getProperty("firstName")).isNotNull();
		assertThat(typeInformation.getProperty("id")).isNotNull();
	}

	@Test // DATACMNS-???
	void domainTypeInformationHoldsSuperTypeAnnotations() {

		assertThat(SubType.typeInfo().findAnnotation(TypeAlias.class)).satisfies(annotations -> {

			assertThat(annotations).hasSize(1);
			assertThat(annotations.get(0).value()).isEqualTo("super");
		});
	}

	@Test // DATACMNS-???
	void domainTypeInformationHoldsSuperTypeAnnotationsAndOwnInBottomUpOrder() {

		assertThat(AliasedSubType.typeInfo().findAnnotation(TypeAlias.class)).satisfies(annotations -> {

			assertThat(annotations).hasSize(2);
			assertThat(annotations.get(0).value()).isEqualTo("sub");
			assertThat(annotations.get(1).value()).isEqualTo("super");
		});
	}

	@Test // DATACMNS-???
	void arrayTypeInformationIsCollectionLike() {

		TypeInformation<?> typeInformation = WithArray.typeInfo().getProperty("objectArray");
		assertThat(typeInformation.isCollectionLike()).isTrue();
	}

	@TypeAlias("super")
	static class AliasedSuperType {

		static ConfigurableTypeInformation<?> typeInfo() {

			return new ConfigurableTypeInformation<AliasedSuperType>(AliasedSuperType.class) {

				{
					addAnnotation(AnnotationAware.typeAliasAnnotation("super"));
				}
			};
		}
	}

	@TypeAlias("sub")
	static class AliasedSubType extends AliasedSuperType {

		static ConfigurableTypeInformation<?> typeInfo() {

			return new ConfigurableTypeInformation(AliasedSubType.class, AliasedSuperType.typeInfo()) {

				{
					addAnnotation(AnnotationAware.typeAliasAnnotation("sub"));
				}
			};
		}
	}

	static class SubType extends AliasedSuperType {

		static ConfigurableTypeInformation<?> typeInfo() {
			return new ConfigurableTypeInformation(SubType.class, AliasedSuperType.typeInfo());
		}
	}

	static class WithArray {

		static ConfigurableTypeInformation<?> typeInfo() {
			return new ConfigurableTypeInformation(WithArray.class) {

				{
					addField(Field.type("objectArray", ArrayTypeInformation.arrayOf(SimpleConfiguredTypes.object())));
				}
			};
		}
	}
}
