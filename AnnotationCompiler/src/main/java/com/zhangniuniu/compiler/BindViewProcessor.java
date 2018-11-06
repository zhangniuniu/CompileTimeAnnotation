package com.zhangniuniu.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * @author：zhangyong
 * @email：zhangyonglncn@gmail.com
 * @create_time: 05/11/2018 15:18
 * @description：
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes("com.zhangniuniu.compiler.BindView")
public class BindViewProcessor extends AbstractProcessor {

    //创建java文件
    private Filer mFiler;
    //打印log
    private Messager mMessager;
    private Elements mElementUtils;
    private Types mTypeUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        mFiler = processingEnvironment.getFiler();
        mMessager = processingEnvironment.getMessager();
        mElementUtils = processingEnvironment.getElementUtils();
        mTypeUtils = processingEnvironment.getTypeUtils();
    }

    private static final String SUFFIX = "$$STUDY";

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        //将使用注解bindview的class集合存储，然后统一处理
        Map<Element, List<Element>> elementListMap = new LinkedHashMap<>();

        for (Element mElement : roundEnvironment.getElementsAnnotatedWith(BindView.class)) {

            if (elementListMap.containsKey(mElement.getEnclosingElement())) {
                elementListMap.get(mElement.getEnclosingElement()).add(mElement);
            } else {
                List<Element> childElements = new ArrayList<>();
                childElements.add(mElement);
                elementListMap.put(mElement.getEnclosingElement(), childElements);
            }

        }

        for (Map.Entry<Element, List<Element>> entryParent : elementListMap.entrySet()) {
            //获取到class Element
            TypeElement classElement = (TypeElement) entryParent.getKey();
            //类名
            String className = classElement.getSimpleName().toString();
            //包名
            PackageElement mPackageElement = mElementUtils.getPackageOf(classElement);
            String packageName = mPackageElement.getQualifiedName().toString();

            // Build input param name.
            ParameterSpec objectParamSpec = ParameterSpec.builder(TypeName.OBJECT, "target").build();

            MethodSpec.Builder injectMethodBuilder = MethodSpec.methodBuilder("inject")
                    .addAnnotation(Override.class)
                    .addModifiers(PUBLIC)
                    .addParameter(objectParamSpec);

            injectMethodBuilder.addStatement("$T substitute = ($T)target", ClassName.get(classElement), ClassName.get(classElement));
            for (int i = 0; i < entryParent.getValue().size(); i++) {
                VariableElement variableElement = (VariableElement) entryParent.getValue().get(i);
                BindView annotation = variableElement.getAnnotation(BindView.class);
                //参数名
                String variableName = variableElement.getSimpleName().toString();
                //属性类型
                TypeMirror typeMirror = variableElement.asType();
                injectMethodBuilder.addStatement("substitute." + variableName + " = ($T)substitute.findViewById(" + annotation.value() + ")", ClassName.get(typeMirror));
            }

            //和注入的Activity同package下 生成文件，属性可直接赋值，否则需要属性public
            TypeSpec typeSpec = TypeSpec.classBuilder(classElement.getSimpleName() + SUFFIX)
                    .addSuperinterface(ClassName.get(IViewInject.class))
                    .addModifiers(Modifier.FINAL, Modifier.PUBLIC)
                    .addMethod(injectMethodBuilder.build())  //在类中添加方法
                    .build();
            JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                    .build();
            try {
                javaFile.writeTo(mFiler);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        //该方法返回ture表示该注解已经被处理, 后续不会再有其他处理器处理; 返回false表示仍可被其他处理器处理.
        return true;
    }


}
