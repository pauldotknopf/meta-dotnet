DESCRIPTION = "Dotnet from source-build"
HOMEPAGE = "http://dot.net"
LICENSE = "MIT"
SECTION = "devel"

SRC_URI = "\
  gitsm://github.com/qmlnet/source-build;branch=yocto-v${PV} \
"
SRCREV="${AUTOREV}"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=9fc642ff452b28d62ab19b7eea50dfb9"

S = "${WORKDIR}/git"

inherit dotnet_cmake

DEPENDS = "clang-native lldb libunwind gettext icu openssl util-linux cmake-native ca-certificates-native krb5 curl"
RDEPENDS_${PN} = "libicuuc libicui18n libcurl libuv libssl"

INSANE_SKIP_${PN} += "staticdev file-rdeps textrel"
WARN_QA_remove = "libdir"
SKIP_FILEDEPS_${PN} = "1"

BUILD_CONFIGURATION = "Release"

do_compile() {
	cd ${S}
	unset bindir
	export CURL_CA_BUNDLE="${STAGING_ETCDIR_NATIVE}/ssl/certs/ca-certificates.crt"
	export SOURCE_BUILD_SKIP_SUBMODULE_CHECK=1
	export ROOTFS_DIR=${STAGING_DIR_HOST}
    export CONFIG_DIR=${CMAKE_CONFIG_DIR}
	./build.sh /p:Platform=x64 /p:Configuration=${BUILD_CONFIGURATION} /p:SkipGenerateRootFs=true
}

do_install() {
    install -d ${D}${datadir}/dotnet
    install -d ${D}${bindir}

    cp -dr ${S}/src/core-setup/Bin/obj/*-x64.${BUILD_CONFIGURATION}/combined-framework-host/* ${D}${datadir}/dotnet/
    ln -sf ../share/dotnet/dotnet ${D}${bindir}/dotnet

    chrpath -d ${D}${datadir}/dotnet//shared/Microsoft.NETCore.App/2.1.3/System.Security.Cryptography.Native.OpenSsl.so
    chrpath -d ${D}${datadir}/dotnet//shared/Microsoft.NETCore.App/2.1.3/System.IO.Compression.Native.so
    chrpath -d ${D}${datadir}/dotnet//shared/Microsoft.NETCore.App/2.1.3/System.Net.Security.Native.so
    chrpath -d ${D}${datadir}/dotnet//shared/Microsoft.NETCore.App/2.1.3/System.Net.Http.Native.so
}

FILES_${PN} = "${datadir}/dotnet ${bindir}/dotnet"