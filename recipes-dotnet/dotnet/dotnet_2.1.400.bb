DESCRIPTION = "The .NET Core runtime and SDK"
HOMEPAGE = "http://dot.net/"
LICENSE = "MIT"
SECTION = "devel"

LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=42b611e7375c06a28601953626ab16cb"

SRC_URI = "https://dotnetcli.blob.core.windows.net/dotnet/Sdk/${PV}/dotnet-sdk-${PV}-linux-x64.tar.gz"
SRC_URI[md5sum] = "69493e47a4dfe714e8f75f6b7bf59394"
SRC_URI[sha256sum] = "aeaf16368ed1c455b70338c24e225a02e9616fc02e5209a2fde4f5a5d9a17de7"

DEPENDS += "patchelf-native"
RDEPENDS_${PN} = "libicuuc libicui18n libcurl libuv libssl"

S = "${WORKDIR}/dotnet-sdk-${PV}-linux-x64"

python base_do_unpack() {
    src_uri = (d.getVar('SRC_URI') or "").split()
    if len(src_uri) == 0:
        return

    try:
        fetcher = bb.fetch2.Fetch(src_uri, d)
        fetcher.unpack(d.getVar('WORKDIR') + "/dotnet-sdk-" + d.getVar("PV") + "-linux-x64")
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
}