package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.CC
import com.github.lppedd.cc.api.BODY_EP
import com.github.lppedd.cc.api.CommitBodyProvider
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.github.lppedd.cc.lookupElement.CommitBodyLookupElement
import com.github.lppedd.cc.parser.CommitTokens
import com.github.lppedd.cc.parser.FooterContext.FooterTypeContext
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.psiElement.CommitBodyPsiElement
import com.github.lppedd.cc.safeRunWithCheckCanceled
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class BodyCompletionProvider(
    private val project: Project,
    private val context: FooterTypeContext,
    private val commitTokens: CommitTokens,
) : CompletionProvider<CommitBodyProvider> {
  override val providers: List<CommitBodyProvider> = BODY_EP.getExtensions(project)
  override val stopHere = false

  override fun complete(resultSet: ResultSet) {
    val rs = resultSet.withPrefixMatcher(context.type)
    providers.asSequence()
      .flatMap { provider ->
        safeRunWithCheckCanceled {
          val wrapper = BodyProviderWrapper(project, provider)
          provider.getCommitBodies(
              (commitTokens.type as? ValidToken)?.value,
              (commitTokens.scope as? ValidToken)?.value,
              (commitTokens.subject as? ValidToken)?.value
            )
            .asSequence()
            .take(CC.Provider.MaxItems)
            .map { wrapper to it }
        }
      }
      .mapIndexed { index, (provider, commitBody) ->
        CommitBodyLookupElement(
          index,
          provider,
          CommitBodyPsiElement(project, commitBody),
        )
      }
      .distinctBy(CommitBodyLookupElement::getLookupString)
      .forEach(rs::addElement)
  }
}
