package com.github.lppedd.cc

import com.github.lppedd.cc.annotation.Compatibility
import com.intellij.openapi.util.registry.Registry
import java.lang.reflect.Constructor
import kotlin.LazyThreadSafetyMode.SYNCHRONIZED

/**
 * Required to avoid a crash at startup in 192.* caused by the
 * `registryKey` extension point.
 *
 * @author Edoardo Luppi
 */
@Compatibility(
  minVersionForRemoval = "192.7936",
  replaceWith = "registryKey extention point"
)
@Suppress("unused")
internal object CCRegistry {
  private val registryKey by lazy(SYNCHRONIZED) {
    val clazz = Class.forName("com.intellij.openapi.util.registry.RegistryKeyDescriptor")
    try {
      // 201.3803.32+
      val ctor = clazz.getDeclaredConstructor(
        String::class.java,
        String::class.java,
        String::class.java,
        Boolean::class.java,
        String::class.java,
      )

      ctor.isAccessible = true
      RegistryKeyCreatorNew(ctor)
    } catch (ignored: Exception) {
      val ctor = clazz.getDeclaredConstructor(
        String::class.java,
        String::class.java,
        String::class.java,
        Boolean::class.java,
        Boolean::class.java,
      )

      ctor.isAccessible = true
      RegistryKeyCreatorOld(ctor)
    }
  }

  fun addKeys(vararg keys: RegistryKeyDescriptor) {
    val method = Registry::class.java.getDeclaredMethod("addKeys", List::class.java)
    method.isAccessible = true
    method.invoke(null, keys.map {
      registryKey.create(
        it.name,
        it.description,
        it.defaultValue,
        it.restartRequired,
        it.pluginId,
      )
    })
  }

  internal data class RegistryKeyDescriptor(
      val name: String,
      val description: String,
      val defaultValue: String,
      val restartRequired: Boolean,
      val pluginId: String? = null,
  )

  private interface RegistryKeyCreator {
    fun create(
        name: String,
        description: String,
        defaultValue: String,
        restartRequired: Boolean,
        pluginId: String? = null,
    ): Any
  }

  private class RegistryKeyCreatorNew(private val ctor: Constructor<*>) : RegistryKeyCreator {
    override fun create(
        name: String,
        description: String,
        defaultValue: String,
        restartRequired: Boolean,
        pluginId: String?,
    ): Any = ctor.newInstance(name, description, defaultValue, restartRequired, pluginId)
  }

  private class RegistryKeyCreatorOld(private val ctor: Constructor<*>) : RegistryKeyCreator {
    override fun create(
        name: String,
        description: String,
        defaultValue: String,
        restartRequired: Boolean,
        pluginId: String?,
    ): Any = ctor.newInstance(name, description, defaultValue, restartRequired, pluginId != null)
  }
}
