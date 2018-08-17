package com.kunlunsoft.copyurl.util;

import com.kunlunsoft.copyurl.common.SpringAnnotations;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiModifierList;

import java.util.stream.Stream;

import static com.kunlunsoft.copyurl.common.SpringAnnotations.*;

public class SpringUtil {

    public static boolean containsSpringAnnotation(SpringAnnotations springAnnotation, PsiModifierList modifierList) {
        if (modifierList != null) {
            PsiAnnotation[] annotations = modifierList.getAnnotations();

            return Stream.of(annotations)
                    .map(a -> a.getQualifiedName())
                    .anyMatch(name -> name.equalsIgnoreCase(springAnnotation.getQualifiedName()));
        }

        return false;
    }

    public static boolean isRestMethod(PsiModifierList modifierList) {
        return containsSpringAnnotation(REQUEST_MAPPING_QUALIFIED_NAME, modifierList) ||
                containsSpringAnnotation(GET_MAPPING_QUALIFIED_NAME, modifierList) ||
                containsSpringAnnotation(POST_MAPPING_QUALIFIED_NAME, modifierList) ||
                containsSpringAnnotation(PUT_MAPPING_QUALIFIED_NAME, modifierList) ||
                containsSpringAnnotation(DELETE_MAPPING_QUALIFIED_NAME, modifierList) ||
                containsSpringAnnotation(PATCH_MAPPING_QUALIFIED_NAME, modifierList);
    }
}
