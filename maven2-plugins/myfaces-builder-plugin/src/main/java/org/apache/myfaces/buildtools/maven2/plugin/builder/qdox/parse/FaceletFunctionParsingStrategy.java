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

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.myfaces.buildtools.maven2.plugin.builder.model.FaceletFunctionMeta;
import org.apache.myfaces.buildtools.maven2.plugin.builder.model.Model;
import org.apache.myfaces.buildtools.maven2.plugin.builder.qdox.QdoxHelper;

import com.thoughtworks.qdox.model.JavaAnnotatedElement;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;

/**
 * 
 * @author Leonardo Uribe
 * @since 1.0.10
 *
 */
public class FaceletFunctionParsingStrategy implements JavaClassParsingStrategy
{
    private static final String DOC_FACELET_FUNCTION = "JSFFaceletFunction";

    public void parseClass(JavaClass clazz, Model model)
    {
        List<JavaMethod> methods = clazz.getMethods();
        for (int i = 0; i < methods.size(); ++i)
        {
            JavaMethod method = methods.get(i);

            DocletTag tag = method.getTagByName(DOC_FACELET_FUNCTION);
            if (tag != null)
            {
                Map props = tag.getNamedParameterMap();
                processFaceletFunction(props, (JavaAnnotatedElement)tag.getContext(), clazz,
                        method, model);
            }

            JavaAnnotation anno = QdoxHelper.getAnnotation(method, DOC_FACELET_FUNCTION);
            if (anno != null)
            {
                Map props = anno.getNamedParameterMap();
                processFaceletFunction(props, (JavaAnnotatedElement)anno, clazz,
                        method, model);
            }
        }
    }
    
    private void processFaceletFunction(Map props, JavaAnnotatedElement ctx,
            JavaClass clazz, JavaMethod method, Model model)
    {
        String name = QdoxHelper.getString(clazz, "name", props, null);
        String longDescription = method.getComment();
        String descDflt = QdoxHelper.getFirstSentence(longDescription);
        if ((descDflt == null) || (descDflt.length() < 2))
        {
            descDflt = "no description";
        }
        String shortDescription = QdoxHelper.getString(clazz, "desc", props, descDflt);
        
        // Check for both "class" and "clazz" in order to support
        // doclet and real annotations.
        String classNameOverride = QdoxHelper.getString(clazz, "class", props, null);
        classNameOverride = QdoxHelper.getString(clazz,"clazz",props,classNameOverride);
        
        String signature = QdoxHelper.getString(clazz,"signature",props, null);
        if (signature == null)
        {
            StringBuilder sb = new StringBuilder();
            sb.append(method.getReturnType().getFullyQualifiedName());
            sb.append(' ');
            sb.append(method.getName());
            sb.append('(');
            sb.append(' ');
            List<JavaParameter> jp = method.getParameters();
            for (int i = 0; i < jp.size() ; i++)
            {
                sb.append(jp.get(i).getType().getFullyQualifiedName());
                if (i+1 < jp.size())
                {
                    sb.append(',');
                    sb.append(' ');
                }
            }
            sb.append(')');
            signature = sb.toString();
        }
        
        String declaredSignature = QdoxHelper.getString(clazz,"declaredSignature",props, null);
        if (declaredSignature == null)
        {
            declaredSignature = method.getDeclarationSignature(false);
        }

        FaceletFunctionMeta ffm = new FaceletFunctionMeta();

        // JSF Entity class.
        if (StringUtils.isEmpty(classNameOverride))
        {
            ffm.setSourceClassName(clazz.getFullyQualifiedName());
        }
        else
        {
            ffm.setSourceClassName(classNameOverride);
        }
        
        ffm.setModelId(model.getModelId());
        ffm.setSignature(signature);
        ffm.setDeclaredSignature(declaredSignature);
        ffm.setName(name);
        ffm.setLongDescription(longDescription);
        ffm.setDescription(shortDescription);
        model.addFaceletFunction(ffm);
    }
}
