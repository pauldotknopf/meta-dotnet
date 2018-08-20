DESCRIPTION = "corehost"
HOMEPAGE = "http://dot.net/"
LICENSE = "MIT"
SECTION = "devel"

DEPENDS = "clang-native coreclr corefx cmake-native openssl ca-certificates-native"
RDEPENDS_${PN} = "libssl libicuuc libicui18n libcurl"

SRC_URI = "git://github.com/qmlnet/core-setup.git;nobranch=1 \
           "

SRCREV = "${CORE_SETUP_SRCREV}"
LIC_FILES_CHKSUM = "file://LICENSE.TXT;md5=42b611e7375c06a28601953626ab16cb"
S = "${WORKDIR}/git"

inherit dotnet_cmake

# Silence some QA warnings, let's not patch the build any more
INSANE_SKIP_${PN} += "staticdev ldflags already-stripped"
SKIP_FILEDEPS_${PN} = "1"

include base.inc

FILES_${PN} = "/opt/dotnet"