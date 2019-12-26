package com.yy.mobile.whisperlint.support.api2

import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiModifierListOwner
import com.intellij.psi.PsiNameValuePair

/**
 * @author YvesCheung
 * 2019-12-23
 */
fun PsiModifierListOwner.getAnnotationCompat(fqn: String): PsiAnnotation? {
    return PsiJvmConversionHelper.getListAnnotation(this, fqn)
}

val PsiNameValuePair.attributeNameCompat: String
    get() = PsiJvmConversionHelper.getAnnotationAttributeName(this)