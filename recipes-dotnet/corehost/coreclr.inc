DESCRIPTION = ".NET Core Common Language Runtime (CoreCLR)"
HOMEPAGE = "http://dot.net/"
LICENSE = "MIT"
SECTION = "devel"

# lttng-ust: https://github.com/dotnet/coreclr/issues/11174
DEPENDS = "clang-native lldb libunwind gettext icu openssl util-linux cmake-native lttng-ust ca-certificates-native"
RDEPENDS_${PN} = "libicuuc libicui18n lttng-ust"

SRC_URI = "git://github.com/qmlnet/coreclr.git;nobranch=1 \
           "

SRCREV = "${CORECLR_SRCREV}"
LIC_FILES_CHKSUM = "file://LICENSE.TXT;md5=9fc642ff452b28d62ab19b7eea50dfb9"
S = "${WORKDIR}/git"

inherit dotnet_cmake

include base.inc

do_compile() {
	cd ${S}
	unset bindir
	export YOCTO_CMAKE_FIND_ROOT_PATH=""
	export CONFIG_DIR=${S}/yocto
	export YOCTO_FORCE_RID=${CORE_RUNTIME_ID}
	export ROOTFS_DIR=${STAGING_DIR_HOST}
	export GCC_TOOLCHAIN=${STAGING_BINDIR_TOOLCHAIN}
	export BuildNumberMajor=${CORECLR_BUILD_MAJOR} 
	export BuildNumberMinor=${CORECLR_BUILD_MINOR}
	export CONFIG_DIR=${CMAKE_CONFIG_DIR}
	./build.sh release cross skiptests
}

do_install() {
	export src="${S}/bin/Product/Linux.x64.Release"
	export target="${D}/opt/dotnet/"

	install -d ${target}

	#install -m 0755 ${src}/coreconsole ${D}/%{dotnetfwdir}
	#install -m 0755 ${src}/crossgen ${D}/%{dotnetfwdir}
	#install -m 0755 ${src}/ilasm ${D}/%{dotnetfwdir}
	#install -m 0755 ${src}/ildasm ${D}/%{dotnetfwdir}
	install -m 0755 ${src}/corerun ${target}
	install -m 0755 ${src}/libclrjit.so ${target}
	install -m 0755 ${src}/libcoreclr.so ${target}
	install -m 0755 ${src}/libdbgshim.so ${target}
	install -m 0755 ${src}/libmscordaccore.so ${target}
	install -m 0755 ${src}/libmscordbi.so ${target}
	#install -m 0755 ${src}/libprotojit.so ${target}
	install -m 0755 ${src}/libsosplugin.so ${target}
	install -m 0755 ${src}/libsos.so ${target}
	#install -m 0755 ${src}/libsuper* ${target}
	#install -m 0755 ${src}/mcs ${target}
	install -m 0755 ${src}/SOS.NETCore.dll ${target}
	#install -m 0755 ${src}/superpmi ${target}
	install -m 0755 ${src}/System.Globalization.Native.so ${target}
	install -m 0755 ${src}/System.Private.CoreLib.dll ${target}

	# Create dev package
	install -d ${D}/opt/dotnet-nupkg/
	for i in `ls ${src}/.nuget/pkg/*.nupkg`
	do
		install -m 644 ${i} ${D}/opt/dotnet-nupkg/
	done
}

SKIP_FILEDEPS_${PN} = "1"

FILES_${PN} = "/opt/dotnet"
FILES_${PN}-dev = "/opt/dotnet-nupkg"

sysroot_stage_all_append () {
    sysroot_stage_dir ${D}/opt/dotnet-nupkg ${SYSROOT_DESTDIR}/opt/dotnet-nupkg
}
