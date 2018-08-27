DESCRIPTION = "A global cache of shared NuGet packages."
HOMEPAGE = "http://dot.net/"
LICENSE = "MIT"
SECTION = "devel"

LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28"

inherit dotnetnative

do_install() {
    echo test
    dotnet --version
}