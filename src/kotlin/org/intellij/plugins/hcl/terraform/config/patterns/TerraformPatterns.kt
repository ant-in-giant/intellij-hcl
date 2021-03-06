/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.plugins.hcl.terraform.config.patterns

import com.intellij.openapi.util.text.StringUtil
import com.intellij.patterns.*
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.intellij.plugins.hcl.psi.*
import org.intellij.plugins.hcl.terraform.config.TerraformFileType
import org.intellij.plugins.hcl.terraform.config.TerraformLanguage

object TerraformPatterns {
  val TerraformVariablesFile: PsiFilePattern.Capture<HCLFile> =
      PlatformPatterns.psiFile(HCLFile::class.java)
          .withLanguage(TerraformLanguage)
          .inVirtualFile(PlatformPatterns.virtualFile().withExtension(TerraformFileType.TFVARS_EXTENSION))
  val TerraformConfigFile: PsiFilePattern.Capture<HCLFile> =
      PlatformPatterns.psiFile(HCLFile::class.java)
          .withLanguage(TerraformLanguage)
          .andNot(TerraformVariablesFile)
  val ConfigOverrideFile: PsiFilePattern.Capture<HCLFile> =
      PlatformPatterns.psiFile(HCLFile::class.java)
          .and(TerraformConfigFile)
          .inVirtualFile(
              PlatformPatterns.virtualFile().withName(StandardPatterns.string().with(object : PatternCondition<String?>("Terraform override file name") {
                override fun accepts(t: String, context: ProcessingContext?): Boolean {
                  val suffix = "override." + TerraformFileType.DEFAULT_EXTENSION
                  if (!t.endsWith(suffix)) return false
                  // previous line enforces t.length >= suffix.length
                  return t.length == suffix.length || t[t.length - suffix.length - 1] == '_'
                }
              })))

  val RootBlock: PsiElementPattern.Capture<HCLBlock> =
      PlatformPatterns.psiElement(HCLBlock::class.java)
          .withParent(TerraformConfigFile)

  val ModuleRootBlock: PsiElementPattern.Capture<HCLBlock> =
      PlatformPatterns.psiElement(HCLBlock::class.java)
          .and(RootBlock)
          .with(createBlockPattern("module"))

  val VariableRootBlock: PsiElementPattern.Capture<HCLBlock> =
      PlatformPatterns.psiElement(HCLBlock::class.java)
          .and(RootBlock)
          .with(createBlockPattern("variable"))

  val OutputRootBlock: PsiElementPattern.Capture<HCLBlock> =
      PlatformPatterns.psiElement(HCLBlock::class.java)
          .and(RootBlock)
          .with(createBlockPattern("output"))

  val ResourceRootBlock: PsiElementPattern.Capture<HCLBlock> =
      PlatformPatterns.psiElement(HCLBlock::class.java)
          .and(RootBlock)
          .with(createBlockPattern("resource"))

  val DataSourceRootBlock: PsiElementPattern.Capture<HCLBlock> =
      PlatformPatterns.psiElement(HCLBlock::class.java)
          .and(RootBlock)
          .with(createBlockPattern("data"))

  val ProviderRootBlock: PsiElementPattern.Capture<HCLBlock> =
      PlatformPatterns.psiElement(HCLBlock::class.java)
          .withParent(TerraformConfigFile)
          .with(createBlockPattern("provider"))

  val TerraformRootBlock: PsiElementPattern.Capture<HCLBlock> =
      PlatformPatterns.psiElement(HCLBlock::class.java)
          .and(RootBlock)
          .with(createBlockPattern("terraform"))

  val LocalsRootBlock: PsiElementPattern.Capture<HCLBlock> =
      PlatformPatterns.psiElement(HCLBlock::class.java)
          .and(RootBlock)
          .with(createBlockPattern("locals"))

  val Backend: PsiElementPattern.Capture<HCLBlock> =
      PlatformPatterns.psiElement(HCLBlock::class.java)
          .with(createBlockPattern("backend"))
          .withSuperParent(2, TerraformRootBlock)

  val ModuleWithEmptySource: PsiElementPattern.Capture<HCLBlock> =
      PlatformPatterns.psiElement(HCLBlock::class.java)
          .and(ModuleRootBlock)
          .with(object: PatternCondition<HCLBlock?>("ModuleWithEmptySource") {
            override fun accepts(t: HCLBlock, context: ProcessingContext?): Boolean {
              val source = t.`object`?.findProperty("source")?.value as? HCLStringLiteral ?: return true
              return StringUtil.isEmptyOrSpaces(source.value)
            }
          })

  val IsBlockNameIdentifier: PatternCondition<PsiElement> = object : PatternCondition<PsiElement>("IsBlockNameIdentifier") {
    override fun accepts(t: PsiElement, context: ProcessingContext?): Boolean {
      val parent = t.parent as? HCLBlock ?: return false
      return parent.nameIdentifier === t
    }
  }

  private fun createBlockPattern(type: String): PatternCondition<HCLBlock?> {
    return object : PatternCondition<HCLBlock?>("HCLBlock($type)") {
      override fun accepts(t: HCLBlock, context: ProcessingContext?): Boolean {
        return t.getNameElementUnquoted(0) == type
      }
    }
  }

  fun propertyWithName(name: String): PsiElementPattern.Capture<HCLProperty> {
    return PlatformPatterns.psiElement(HCLProperty::class.java).with(object : PatternCondition<HCLProperty>("HCLProperty($name)") {
      override fun accepts(t: HCLProperty, context: ProcessingContext?): Boolean {
        return name == t.name
      }
    })
  }
}
