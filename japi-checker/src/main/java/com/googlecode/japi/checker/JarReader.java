/*
 * Copyright 2012 William Bernardet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.japi.checker;

import com.googlecode.japi.checker.model.ClassData;
import com.googlecode.japi.checker.model.FieldData;
import com.googlecode.japi.checker.model.MethodData;
import com.googlecode.japi.checker.model.TypeParameterData;

import org.objectweb.asm.ClassReader;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class JarReader<C extends ClassData> extends AbstractClassReader<C> {
	private File file;
	private ClassDumper<C> dumper;

	public JarReader(File file, ClassDataLoader<C> loader) {
		this.file = file;
		this.dumper = new ClassDumper<C>(loader);
	}

	public JarReader(File file, ClassDataLoader<C> loader,
					 Class<C> classClass,
					 Class<? extends FieldData> fieldClass,
					 Class<? extends MethodData> methodClass,
					 Class<? extends TypeParameterData> typeParameterClass) {
		this.file = file;
		this.dumper = new ClassDumper<C>(loader, classClass, fieldClass, methodClass, typeParameterClass);
	}

	@Override
	public void read() throws IOException {
		this.clear();
		FileInputStream fis = null;
		ZipInputStream zis = null;
		try {
			fis = new FileInputStream(this.file);
			zis = new ZipInputStream(new BufferedInputStream(fis));
			ZipEntry entry = null;
			byte buffer[] = new byte[2048];
			int count = 0;
			while ((entry = zis.getNextEntry()) != null) {
				if (entry.getName().endsWith(".class")) {

					ByteArrayOutputStream os = new ByteArrayOutputStream();
					while ((count = zis.read(buffer)) != -1) {
						os.write(buffer, 0, count);
					}
					ClassReader cr = new ClassReader(os.toByteArray());
					cr.accept(dumper, 0);
					C clazz = dumper.getClazz();
					if (clazz != null) {
						this.put(entry.getName(), clazz);
					}
				}
			}
		} finally {
			if (fis != null) {
				fis.close();
			}
			if (zis != null) {
				zis.close();
			}
		}
	}

}
