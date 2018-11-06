# 编译时注解
## APT

APT(Annotation Processing Tool)是一种处理注释的工具,它对源代码文件进行检测找出其中的Annotation，使用Annotation进行额外的处理。
Annotation处理器在处理Annotation时可以根据源文件中的Annotation生成额外的源文件和其它的文件(文件具体内容由Annotation处理器的编写者决定),APT还会编译生成的源文件和原来的源文件，将它们一起生成class文件。

### Compiler Module

1. 编译的jdk版本为1.7
2. AutoService 主要的作用是注解 processor 类，并对其生成 META-INF 的配置信息，引入Google的包实现(com.google.auto.service:auto-service:1.0-rc2)
3. JavaPoet用了创建Java类，相比直接用StringBuilder拼接，好处在与规范的文档，最重要的是自动 import

```java
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


        return false;
    }


}
```

