DEPENDS_prepend = "cmake-native "

CMAKE_CONFIG_DIR="${WORKDIR}/cmake-config"

# CMake expects target architectures in the format of uname(2),
# which do not always match TARGET_ARCH, so all the necessary
# conversions should happen here.
def map_target_arch_to_uname_arch(target_arch):
    if target_arch == "powerpc":
        return "ppc"
    if target_arch == "powerpc64":
        return "ppc64"
    return target_arch

dotnet_cmake_do_generate_toolchain_file() {
	if [ "${BUILD_SYS}" = "${HOST_SYS}" ]; then
		cmake_crosscompiling="set( CMAKE_CROSSCOMPILING FALSE )"
	fi
    #set(${ARGV0} ${ARGV1} CACHE STRING "Result from TRY_RUN" FORCE)
    marcro_set=$(echo set'(''$'{ARGV0} '$'{ARGV1} CACHE STRING '"'Result from TRY_RUN'"' FORCE')')
    rm -rf ${CMAKE_CONFIG_DIR}
    mkdir ${CMAKE_CONFIG_DIR}
    cat > ${CMAKE_CONFIG_DIR}/tryrun.cmake <<EOF
macro(set_cache_value)
  $marcro_set
endmacro()

set_cache_value(FILE_OPS_CHECK_FERROR_OF_PREVIOUS_CALL_EXITCODE 1)
set_cache_value(GETPWUID_R_SETS_ERRNO_EXITCODE 0)
set_cache_value(HAS_POSIX_SEMAPHORES_EXITCODE 0)
set_cache_value(HAVE_CLOCK_MONOTONIC_COARSE_EXITCODE 0)
set_cache_value(HAVE_CLOCK_MONOTONIC_EXITCODE 0)
set_cache_value(HAVE_CLOCK_THREAD_CPUTIME_EXITCODE 0)
set_cache_value(HAVE_CLOCK_REALTIME_EXITCODE 0)
set_cache_value(HAVE_COMPATIBLE_ACOS_EXITCODE 0)
set_cache_value(HAVE_COMPATIBLE_ASIN_EXITCODE 0)
set_cache_value(HAVE_COMPATIBLE_ATAN2_EXITCODE 0)
set_cache_value(HAVE_COMPATIBLE_LOG10_EXITCODE 0)
set_cache_value(HAVE_COMPATIBLE_LOG_EXITCODE 0)
set_cache_value(HAVE_COMPATIBLE_POW_EXITCODE 0)
set_cache_value(HAVE_LARGE_SNPRINTF_SUPPORT_EXITCODE 0)
set_cache_value(HAVE_MMAP_DEV_ZERO_EXITCODE 0)
set_cache_value(HAVE_PROCFS_CTL_EXITCODE 1)
set_cache_value(HAVE_PROCFS_MAPS_EXITCODE 0)
set_cache_value(HAVE_PROCFS_STATUS_EXITCODE 0)
set_cache_value(HAVE_PROCFS_STAT_EXITCODE 0)
set_cache_value(HAVE_SCHED_GETCPU_EXITCODE 0)
set_cache_value(HAVE_SCHED_GET_PRIORITY_EXITCODE 0)
set_cache_value(HAVE_VALID_NEGATIVE_INF_POW_EXITCODE 0)
set_cache_value(HAVE_VALID_POSITIVE_INF_POW_EXITCODE 0)
set_cache_value(HAVE_WORKING_CLOCK_GETTIME_EXITCODE 0)
set_cache_value(HAVE_WORKING_GETTIMEOFDAY_EXITCODE 0)
set_cache_value(ONE_SHARED_MAPPING_PER_FILEREGION_PER_PROCESS_EXITCODE 1)
set_cache_value(PTHREAD_CREATE_MODIFIES_ERRNO_EXITCODE 1)
set_cache_value(REALPATH_SUPPORTS_NONEXISTENT_FILES_EXITCODE 1)
set_cache_value(SEM_INIT_MODIFIES_ERRNO_EXITCODE 1)
set_cache_value(SSCANF_CANNOT_HANDLE_MISSING_EXPONENT_EXITCODE 1)
set_cache_value(SSCANF_SUPPORT_ll_EXITCODE 0)
set_cache_value(UNGETC_NOT_RETURN_EOF_EXITCODE 0)
set_cache_value(HAVE_FUNCTIONAL_PTHREAD_ROBUST_MUTEXES_EXITCODE 0)
EOF
	cat > ${CMAKE_CONFIG_DIR}/toolchain.cmake <<EOF
# CMake system name must be something like "Linux".
# This is important for cross-compiling.
$cmake_crosscompiling
set( CMAKE_SYSTEM_NAME `echo ${TARGET_OS} | sed -e 's/^./\u&/' -e 's/^\(Linux\).*/\1/'` )
set( CMAKE_SYSTEM_PROCESSOR ${@map_target_arch_to_uname_arch(d.getVar('TARGET_ARCH'))} )
set( TOOLCHAIN ${TARGET_SYS} )

add_compile_options(--sysroot=${STAGING_DIR_HOST})
add_compile_options(-target ${TARGET_SYS})

# only search in the paths provided so cmake doesnt pick
# up libraries and tools from the native build machine
set( CMAKE_FIND_ROOT_PATH ${STAGING_DIR_HOST})
set( CMAKE_FIND_ROOT_PATH_MODE_PACKAGE ONLY )
set( CMAKE_FIND_ROOT_PATH_MODE_PROGRAM BOTH )
set( CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY )
set( CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY )

set( CMAKE_INSTALL_RPATH  )

# Use native cmake modules
list(APPEND CMAKE_MODULE_PATH "${STAGING_DATADIR}/cmake/Modules/")

# add for non /usr/lib libdir, e.g. /usr/lib64
set( CMAKE_LIBRARY_PATH ${libdir} ${base_libdir})

EOF
}

addtask generate_toolchain_file after do_patch before do_configure

EXPORT_FUNCTIONS do_generate_toolchain_file
