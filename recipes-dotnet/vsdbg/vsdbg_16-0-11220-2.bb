DESCRIPTION = "The .NET debugger"
HOMEPAGE = "https://github.com/Microsoft/MIEngine/wiki/Offroad-Debugging-of-.NET-Core-on-Linux---OSX-from-Visual-Studio"
LICENSE = "Proprietary"
SECTION = "devel"

LIC_FILES_CHKSUM = "file://license.txt;md5=b84c3bcd1eb089d7eff9fb20768b47a3"

SRC_URI = "https://vsdebugger.azureedge.net/vsdbg-${PV}/vsdbg-linux-x64.zip"
SRC_URI[md5sum] = "368594ec358ce1a972f71c506d7b97a2"
SRC_URI[sha256sum] = "3e917d83500723545deecf54ceedda5f8bfaa422b3e8954732719d5d45041830"

DEPENDS += "patchelf-native"

RDEPENDS_${PN} += "zlib libcurl krb5"

S = "${WORKDIR}/vsdbg-linux-x64-${PV}"

python base_do_unpack() {
    src_uri = (d.getVar('SRC_URI') or "").split()
    if len(src_uri) == 0:
        return
    try:
        fetcher = bb.fetch2.Fetch(src_uri, d)
        fetcher.unpack(d.getVar('S'))
    except bb.fetch2.BBFetchException as e:
        bb.fatal(str(e))
}

do_install() {
    install -d ${D}${datadir}/vsdbg
    cp -av --no-preserve=ownership ${S}/* ${D}${datadir}/vsdbg
    chmod +x ${D}${datadir}/vsdbg/vsdbg
    patchelf --set-interpreter /lib/ld-linux-x86-64.so.2 ${D}${datadir}/vsdbg/vsdbg
}

# Add .NET libs to another package so that we can disable some
# validation checks. The sep package is so that the validation
# checks stay in for the normal artifacts (/opt/Abra/bin, etc).
PACKAGES =+ "${PN}-net"
RDEPENDS_${PN} += "${PN}-net"
SKIP_FILEDEPS_${PN}-net = "1"
FILES_${PN}-net += "${datadir}/vsdbg/*.dll ${datadir}/vsdbg/**/*.dll"

INSANE_SKIP_${PN} += "libdir ldflags"