DESCRIPTION = "The .NET Core runtime and SDK"
HOMEPAGE = "http://dot.net/"
LICENSE = "MIT"
SECTION = "devel"

LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=42b611e7375c06a28601953626ab16cb"

SRC_URI = "https://dotnetcli.blob.core.windows.net/dotnet/Sdk/${PV}/dotnet-sdk-${PV}-linux-x64.tar.gz"
SRC_URI[md5sum] = "2b63831353bb95bb2d577c48fa8c8b63"
SRC_URI[sha256sum] = "cf26fcd1938eccfa80120e917ffd9fdc4b478415d754db619d88f54e91767b2d"

SRC_URI_append_class-native = " file://dotnet-native"

DEPENDS += "patchelf-native"
RDEPENDS_${PN} = "libicuuc libicui18n libcurl libuv libssl"
RDEPENDS_${PN}_class-native = "icu-native curl-native libuv-native openssl-native"

S = "${WORKDIR}/dotnet-sdk-${PV}-linux-x64"

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

INSANE_SKIP_${PN} += "already-stripped file-rdeps staticdev"
SKIP_FILEDEPS_${PN} = '1'

do_install() {
    # Change the interpreter for all the executables.
    # The onces in the tarball are "/lib64/ld-linux-x86-64.so.2", we need "/lib/ld-linux-x86-64.so.2"
    find -type f -executable -exec sh -c "file -i '{}' | grep -q 'x-executable; charset=binary'" \; -print | xargs patchelf --set-interpreter /lib/ld-linux-x86-64.so.2

	install -d ${D}${datadir}/dotnet
	cp -rp ${S}/* ${D}${datadir}/dotnet

    install -d ${D}${bindir}
    ln -s ../..${datadir}/dotnet/dotnet ${D}${bindir}/dotnet

    # The installers delivered from Microsoft have uid of 1000.
    chown -R root:root ${D}
}

do_install_append_class-native() {
    # Use our custom dotnet script that unloads psuedo
    rm ${D}${bindir}/dotnet
    cp ${S}/dotnet-native ${D}${bindir}/dotnet
    rm ${D}${datadir}/dotnet/dotnet-native
}

INSANE_SKIP_${PN} += "libdir"
INSANE_SKIP_${PN}-dev += "libdir"
INSANE_SKIP_${PN}-dbg += "libdir"

FILES_${PN}-dev += "/usr/share/dotnet/sdk"

BBCLASSEXTEND = "native"