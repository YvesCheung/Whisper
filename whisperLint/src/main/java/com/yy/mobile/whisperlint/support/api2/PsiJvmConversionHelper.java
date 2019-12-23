package com.yy.mobile.whisperlint.support.api2;

import com.intellij.lang.jvm.JvmClassKind;
import com.intellij.lang.jvm.JvmModifier;
import com.intellij.lang.jvm.types.JvmReferenceType;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnonymousClass;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.psi.PsiReferenceList;
import com.intellij.psi.PsiType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * copy from {@link com.intellij.psi.PsiJvmConversionHelper}
 */
class PsiJvmConversionHelper {
    private static final Map<JvmModifier, String> MODIFIERS;

    PsiJvmConversionHelper() {
    }

    @NotNull
    static PsiAnnotation[] getListAnnotations(@NotNull PsiModifierListOwner modifierListOwner) {
        PsiModifierList list = modifierListOwner.getModifierList();
        return list == null ? PsiAnnotation.EMPTY_ARRAY : list.getAnnotations();
    }

    @Nullable
    static PsiAnnotation getListAnnotation(@NotNull PsiModifierListOwner modifierListOwner, @NotNull String fqn) {
        PsiModifierList list = modifierListOwner.getModifierList();
        return list == null ? null : list.findAnnotation(fqn);
    }

    static boolean hasListAnnotation(@NotNull PsiModifierListOwner modifierListOwner, @NotNull String fqn) {
        PsiModifierList list = modifierListOwner.getModifierList();
        return list != null && list.hasAnnotation(fqn);
    }

    static boolean hasListModifier(@NotNull PsiModifierListOwner modifierListOwner, @NotNull JvmModifier modifier) {
        return modifierListOwner.hasModifierProperty(MODIFIERS.get(modifier));
    }

    @NotNull
    static JvmClassKind getJvmClassKind(@NotNull PsiClass psiClass) {
        JvmClassKind var10000;
        if (psiClass.isAnnotationType()) {
            var10000 = JvmClassKind.ANNOTATION;
            return var10000;
        } else if (psiClass.isInterface()) {
            var10000 = JvmClassKind.INTERFACE;
            return var10000;
        } else if (psiClass.isEnum()) {
            var10000 = JvmClassKind.ENUM;
            return var10000;
        } else {
            var10000 = JvmClassKind.CLASS;
            return var10000;
        }
    }

    @Nullable
    static JvmReferenceType getClassSuperType(@NotNull PsiClass psiClass) {
        if (psiClass.isInterface()) {
            return null;
        } else if (psiClass.isEnum()) {
            return PsiType.getTypeByName("java.lang.Enum", psiClass.getProject(), psiClass.getResolveScope());
        } else if (psiClass instanceof PsiAnonymousClass) {
            PsiClassType baseClassType = ((PsiAnonymousClass) psiClass).getBaseClassType();
            PsiClass baseClass = baseClassType.resolve();
            return baseClass != null && baseClass.isInterface() ? PsiType.getJavaLangObject(psiClass.getManager(), psiClass.getResolveScope()) : baseClassType;
        } else if ("java.lang.Object".equals(psiClass.getQualifiedName())) {
            return null;
        } else {
            PsiClassType[] extendsTypes = psiClass.getExtendsListTypes();
            return extendsTypes.length != 1 ? PsiType.getJavaLangObject(psiClass.getManager(), psiClass.getResolveScope()) : extendsTypes[0];
        }
    }

    @NotNull
    static JvmReferenceType[] getClassInterfaces(@NotNull PsiClass psiClass) {
        JvmReferenceType[] var4;
        if (psiClass instanceof PsiAnonymousClass) {
            PsiClassType baseClassType = ((PsiAnonymousClass) psiClass).getBaseClassType();
            PsiClass baseClass = baseClassType.resolve();
            if (baseClass != null && baseClass.isInterface()) {
                var4 = new JvmReferenceType[]{baseClassType};
                return var4;
            } else {
                var4 = JvmReferenceType.EMPTY_ARRAY;
                return var4;
            }
        } else {
            PsiReferenceList referenceList = psiClass.isInterface() ? psiClass.getExtendsList() : psiClass.getImplementsList();
            if (referenceList == null) {
                var4 = JvmReferenceType.EMPTY_ARRAY;
                return var4;
            } else {
                PsiClassType[] var10000 = referenceList.getReferencedTypes();
                return var10000;
            }
        }
    }

    @NotNull
    static String getAnnotationAttributeName(@NotNull PsiNameValuePair pair) {
        String name = pair.getName();
        return name == null ? "value" : name;
    }


    static {
        Map<JvmModifier, String> modifiers = new EnumMap<>(JvmModifier.class);
        modifiers.put(JvmModifier.PUBLIC, "public");
        modifiers.put(JvmModifier.PROTECTED, "protected");
        modifiers.put(JvmModifier.PRIVATE, "private");
        modifiers.put(JvmModifier.PACKAGE_LOCAL, "packageLocal");
        modifiers.put(JvmModifier.STATIC, "static");
        modifiers.put(JvmModifier.ABSTRACT, "abstract");
        modifiers.put(JvmModifier.FINAL, "final");
        modifiers.put(JvmModifier.NATIVE, "native");
        modifiers.put(JvmModifier.SYNCHRONIZED, "synchronized");
        modifiers.put(JvmModifier.STRICTFP, "strictfp");
        modifiers.put(JvmModifier.TRANSIENT, "transient");
        modifiers.put(JvmModifier.VOLATILE, "volatile");
        modifiers.put(JvmModifier.TRANSITIVE, "transitive");
        MODIFIERS = Collections.unmodifiableMap(modifiers);
    }
}