/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.myfaces.buildtools.maven2.plugin.builder.qdox.parse;

import java.util.Map;

import org.apache.myfaces.buildtools.maven2.plugin.builder.model.ConverterMeta;
import org.apache.myfaces.buildtools.maven2.plugin.builder.model.Model;
import org.apache.myfaces.buildtools.maven2.plugin.builder.qdox.QdoxHelper;

import com.thoughtworks.qdox.model.JavaAnnotatedElement;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;

/**
 * 
 * @author Leonardo Uribe
 * @since 1.0.9
 *
 */
public class ConverterParsingStrategy extends ClassMetaPropertyParsingStrategy
{
    private static final String DOC_CONVERTER = "JSFConverter";

    public void parseClass(JavaClass clazz, Model model)
    {
        DocletTag tag;
        JavaAnnotation anno;
        // converters
        tag = clazz.getTagsByName(DOC_CONVERTER, false).get(0);
        if (tag != null)
        {
            Map props = tag.getNamedParameterMap();
            processConverter(props, (JavaAnnotatedElement)tag.getContext(), clazz, model);
        }
        anno = QdoxHelper.getAnnotation(clazz, DOC_CONVERTER);
        if (anno != null)
        {
            Map props = anno.getNamedParameterMap();
            processConverter(props, (JavaAnnotatedElement)anno, clazz, model);
        }
    }
    
    private void processConverter(Map props, JavaAnnotatedElement ctx,
            JavaClass clazz, Model model)
    {
        String longDescription = clazz.getComment();
        String descDflt = QdoxHelper.getFirstSentence(longDescription);
        if ((descDflt == null) || (descDflt.length() < 2))
        {
            descDflt = "no description";
        }
        String shortDescription = QdoxHelper.getString(clazz, "desc", props, descDflt);

        String converterIdDflt = null;
        JavaField fieldConverterId = clazz
                .getFieldByName("CONVERTER_ID");
        if (fieldConverterId != null)
        {
            String value = fieldConverterId.getInitializationExpression();
            converterIdDflt = QdoxHelper.clean(value.substring(value.indexOf('"')));
        }        
        String converterId = QdoxHelper.getString(clazz, "id", props, converterIdDflt);

        // Check for both "class" and "clazz" in order to support
        // doclet and real annotations.
        String classNameOverride = QdoxHelper.getString(clazz, "class", props, null);
        classNameOverride = QdoxHelper.getString(clazz,"clazz",props,classNameOverride);
        
        String componentName = QdoxHelper.getString(clazz, "name", props, null);
        String bodyContent = QdoxHelper.getString(clazz, "bodyContent", props, null);
        String tagClass = QdoxHelper.getString(clazz, "tagClass", props, null);
        String tagSuperclass = QdoxHelper.getString(clazz, "tagSuperclass", props, null);
        String tagHandler = QdoxHelper.getString(clazz, "tagHandler", props, null);
        String serialuidtag = QdoxHelper.getString(clazz, "serialuidtag", props, null);
        Boolean configExcluded = QdoxHelper.getBoolean(clazz,"configExcluded",props,null);   
        Boolean evaluateELOnExecution = QdoxHelper.getBoolean(clazz,"evaluateELOnExecution",props,null);

        ConverterMeta converter = new ConverterMeta();
        initClassMeta(model, clazz, converter, classNameOverride);
        converter.setName(componentName);
        converter.setBodyContent(bodyContent);
        converter.setTagClass(tagClass);
        converter.setTagSuperclass(tagSuperclass);
        converter.setTagHandler(tagHandler);
        converter.setConverterId(converterId);
        converter.setDescription(shortDescription);
        converter.setLongDescription(longDescription);
        converter.setSerialuidtag(serialuidtag);
        converter.setConfigExcluded(configExcluded);
        converter.setEvaluateELOnExecution(evaluateELOnExecution);
        
        // Now here walk the component looking for property annotations.
        processComponentProperties(clazz, converter);
        
        model.addConverter(converter);
    }
}
