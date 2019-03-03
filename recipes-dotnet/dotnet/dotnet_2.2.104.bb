DESCRIPTION = "The .NET Core runtime and SDK"
HOMEPAGE = "http://dot.net/"
LICENSE = "MIT"
SECTION = "devel"

LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=42b611e7375c06a28601953626ab16cb"

SRC_URI = "https://dotnetcli.blob.core.windows.net/dotnet/Sdk/${PV}/dotnet-sdk-${PV}-linux-x64.tar.gz"
SRC_URI[md5sum] = "07d9d978f8a84684f3adb6c2d5952385"
SRC_URI[sha256sum] = "d33cdcd784ed5503ab66d771cc8d28d5493bc8bbc596e4276db9856453c069e3"

SRC_URI_append_class-native = " file://dotnet-native"

DEPENDS += "patchelf-native jq-native"
RDEPENDS_${PN} = "libicuuc libicui18n libcurl libssl krb5"
RDEPENDS_${PN}_class-native = "icu-native curl-native openssl-native"

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

INSANE_SKIP_${PN} += "already-stripped file-rdeps staticdev libdir"
INSANE_SKIP_${PN}-dbg += "libdir"
INSANE_SKIP_${PN}-dev += "already-stripped file-rdeps staticdev libdir"
SKIP_FILEDEPS_${PN} = '1'
SKIP_FILEDEPS_${PN}-dev = '1'

do_install() {
    # Change the interpreter for all the executables.
    # The onces in the tarball are "/lib64/ld-linux-x86-64.so.2", we need "/lib/ld-linux-x86-64.so.2"
    find -type f -executable -exec sh -c "file -i '{}' | grep -q 'x-executable; charset=binary'" \; -print | xargs -L1 patchelf --set-interpreter /lib/ld-linux-x86-64.so.2

    # Remove libcoreclrtraceptprovider.so
    rm -f ${S}/shared/Microsoft.NETCore.App/2.2.2/libcoreclrtraceptprovider.so
    # Remove the reference to libcoreclrtraceptprovider.so in the deps.json file.
    rm -f ${WORKDIR}/tmp.json
    cat ${S}/shared/Microsoft.NETCore.App/2.2.2/Microsoft.NETCore.App.deps.json | \
      jq 'del(."targets".".NETCoreApp,Version=v2.2/linux-x64"."runtime.linux-x64.Microsoft.NETCore.App/2.2.2"."native"."runtimes/linux-x64/native/libcoreclrtraceptprovider.so")' \
      > ${WORKDIR}/tmp.json
    cp ${WORKDIR}/tmp.json ${S}/shared/Microsoft.NETCore.App/2.2.2/Microsoft.NETCore.App.deps.json

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

FILES_${PN}-dev += "/usr/share/dotnet/sdk"

BBCLASSEXTEND = "native"