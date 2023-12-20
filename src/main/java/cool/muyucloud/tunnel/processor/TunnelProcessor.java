package cool.muyucloud.tunnel.processor;

import cool.muyucloud.tunnel.annotation.Tunnel;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;
import java.io.PrintWriter;
import java.util.Set;

@SupportedAnnotationTypes("cool.muyucloud.tunnel.annotation.Tunnel")
public class TunnelProcessor extends AbstractProcessor {
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Tunnel.class);
        for (Element element : elements) {
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement typeElement = (TypeElement) element;
                generateStaticBlock(typeElement);
            }
        }
        return true;
    }

    private String getImpl(TypeElement element) {
        String iPackage = getPackageName(element);
        String i = element.getSimpleName().toString();
        Tunnel annotation = element.getAnnotation(Tunnel.class);
        String impl = annotation.impl();
        if (impl.isEmpty()) {
            impl = i + "Impl";
        } else if (impl.indexOf('.') == -1) {
            impl = iPackage + "." + impl;
        }
        return impl;
    }

    private String getPackageName(TypeElement element) {
        return elementUtils.getPackageOf(element).getQualifiedName().toString();
    }

    private void generateStaticBlock(TypeElement element) {
        String iPackage = getPackageName(element);
        String i = element.getSimpleName().toString();
        String impl = getImpl(element);
        try {
            String iTunnelFull = iPackage + "." + i;
            JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(iTunnelFull + "_TunnelInitializer");
            PrintWriter writer = new PrintWriter(sourceFile.openWriter());
            writer.println("package " + iPackage + ";");
            writer.println("import cool.muyucloud.tunnel.TunnelInitializer;");
            writer.println("public class " + i + "_TunnelInitializer {");
            writer.println("    static {");
            writer.println("        TunnelInitializer.init(" + impl + ");");
            writer.println("    }");
            writer.println("}");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
