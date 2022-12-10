package eu.darken.sdmse.common.areas.modules.system

import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import eu.darken.sdmse.common.areas.DataArea
import eu.darken.sdmse.common.areas.modules.DataAreaModule
import eu.darken.sdmse.common.debug.logging.Logging.Priority.INFO
import eu.darken.sdmse.common.debug.logging.Logging.Priority.WARN
import eu.darken.sdmse.common.debug.logging.log
import eu.darken.sdmse.common.debug.logging.logTag
import eu.darken.sdmse.common.files.core.APath
import eu.darken.sdmse.common.files.core.GatewaySwitch
import eu.darken.sdmse.common.files.core.local.LocalGateway
import eu.darken.sdmse.common.files.core.local.LocalPath
import javax.inject.Inject

@Reusable
class SystemAppModule @Inject constructor(
    private val gatewaySwitch: GatewaySwitch,
) : DataAreaModule {

    override suspend fun secondPass(firstPass: Collection<DataArea>): Collection<DataArea> {
        val gateway = gatewaySwitch.getGateway(APath.PathType.LOCAL) as LocalGateway

        if (!gateway.hasRoot()) {
            log(TAG, INFO) { "LocalGateway has no root, skipping." }
            return emptySet()
        }

        return firstPass
            .filter { it.type == DataArea.Type.SYSTEM }
            .mapNotNull { area ->
                val path = LocalPath.build(area.path as LocalPath, "app")

                if (!gateway.exists(path, mode = LocalGateway.Mode.ROOT)) {
                    log(TAG, WARN) { "Does not exist: $path" }
                    return@mapNotNull null
                }

                DataArea(
                    type = DataArea.Type.SYSTEM_APP,
                    path = path,
                    userHandle = area.userHandle,
                    flags = area.flags,
                )
            }
    }

    @Module @InstallIn(SingletonComponent::class)
    abstract class DIM {
        @Binds @IntoSet abstract fun mod(mod: SystemAppModule): DataAreaModule
    }

    companion object {
        val TAG: String = logTag("DataArea", "Module", "System", "App")
    }
}