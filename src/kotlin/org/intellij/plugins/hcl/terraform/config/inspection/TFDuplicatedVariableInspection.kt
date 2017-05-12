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
package org.intellij.plugins.hcl.terraform.config.inspection

import com.intellij.codeInspection.*
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import org.intellij.plugins.hcl.psi.HCLBlock
import org.intellij.plugins.hcl.psi.HCLElementVisitor
import org.intellij.plugins.hcl.psi.getNameElementUnquoted
import org.intellij.plugins.hcl.terraform.config.model.getTerraformModule
import org.intellij.plugins.hcl.terraform.config.patterns.TerraformPatterns

class TFDuplicatedVariableInspection : TFDuplicatedInspectionBase() {
  override fun createVisitor(holder: ProblemsHolder): PsiElementVisitor {
    return MyEV(holder)
  }

  inner class MyEV(val holder: ProblemsHolder) : HCLElementVisitor() {
    override fun visitBlock(block: HCLBlock) {
      if (!TerraformPatterns.VariableRootBlock.accepts(block)) return
      if (TerraformPatterns.ConfigOverrideFile.accepts(block.containingFile)) return

      val module = block.getTerraformModule()

      val name = block.getNameElementUnquoted(1) ?: return

      val same = module.getDefinedVariables().filter { name == it.getNameElementUnquoted(1) && !TerraformPatterns.ConfigOverrideFile.accepts(it.containingFile) }
      if (same.isEmpty()) return
      if (same.size == 1) {
        assert(same.first() == block)
        return
      }
      holder.registerProblem(block, "Variable '$name' declared multiple times", ProblemHighlightType.GENERIC_ERROR, *getFixes())
    }
  }

  private fun getFixes(): Array<LocalQuickFix> {
    if (true) return emptyArray()
    return arrayOf(
        DeleteVariableFix,
        RenameVariableFix
    )
  }

  private object DeleteVariableFix : LocalQuickFixBase("Delete variable") {
    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
      // TODO Implement
    }
  }

  private object RenameVariableFix : LocalQuickFixBase("Rename variable") {
    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
      // TODO Implement
    }
  }
}
