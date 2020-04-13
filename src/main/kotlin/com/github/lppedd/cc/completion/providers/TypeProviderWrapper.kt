package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.api.CommitTypeProvider
import com.github.lppedd.cc.api.ProviderPresentation
import com.github.lppedd.cc.completion.Priority
import com.github.lppedd.cc.configuration.CCConfigService
import com.intellij.openapi.project.Project
import java.time.Duration

/**
 * @author Edoardo Luppi
 */
internal class TypeProviderWrapper(
    project: Project,
    private val provider: CommitTypeProvider,
) : ProviderWrapper {
  private val config = CCConfigService.getInstance(project)

  override fun getId(): String =
    provider.getId()

  override fun getPresentation(): ProviderPresentation =
    provider.getPresentation()

  override fun timeout(): Duration =
    provider.timeout()

  override fun getPriority() =
    Priority(config.getProviderOrder(provider))
}
