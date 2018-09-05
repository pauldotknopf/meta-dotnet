OECMAKE_C_COMPILER = "clang"
OECMAKE_CXX_COMPILER ?= "clang"
# Inherit from cmake.bbclass to use the existing functionality.
inherit cmake

CMAKE_CONFIG_DIR="${WORKDIR}/cmake-config"

python() {
    # Prevent progress output
    d.delVarFlag("do_compile", "progress")
}

dotnet_cmake_do_configure() {
    echo 'empty'
}

dotnet_cmake_do_compile()  {
	echo 'empty'
}

dotnet_cmake_do_install() {
	echo 'empty'
}

dotnet_cmake_do_generate_toolchain_config_dir() {
    rm -rf ${CMAKE_CONFIG_DIR}
    mkdir ${CMAKE_CONFIG_DIR}

    # Move over the generated toolchain file.
    cp ${WORKDIR}/toolchain.cmake ${CMAKE_CONFIG_DIR}/toolchain.cmake
    # Disable event tracing.
    # See https://github.com/dotnet/coreclr/issues/15693
    echo "set( FEATURE_EVENT_TRACE 0 )" >> ${CMAKE_CONFIG_DIR}/toolchain.cmake
    echo "set( FEATURE_PERFTRACING 0 )" >> ${CMAKE_CONFIG_DIR}/toolchain.cmake
    echo "set( TOOLCHAIN ${TARGET_SYS} )" >> ${CMAKE_CONFIG_DIR}/toolchain.cmake
    #echo "add_compile_options(--sysroot=${STAGING_DIR_HOST})" >> ${CMAKE_CONFIG_DIR}/toolchain.cmake
    #echo "add_compile_options(-target ${TARGET_SYS})" >> ${CMAKE_CONFIG_DIR}/toolchain.cmake
    sed -i '/set( CMAKE_FIND_ROOT_PATH_MODE_PROGRAM ONLY )/c\set( CMAKE_FIND_ROOT_PATH_MODE_PROGRAM BOTH )' ${CMAKE_CONFIG_DIR}/toolchain.cmake

    #set( CMAKE_C_COMPILER x86_64-poky-linux-gcc )
    #set( CMAKE_CXX_COMPILER x86_64-poky-linux-g++ )
    #set( CMAKE_ASM_COMPILER x86_64-poky-linux-gcc )

    # Create a tryrun.cmake.
    # If this is a native (non-cross) build, leave it empty.
    if [ "${BUILD_SYS}" = "${HOST_SYS}" ]; then
        touch ${CMAKE_CONFIG_DIR}/tryrun.cmake
    else
        marcro_set=$(echo set'(''$'{ARGV0} '$'{ARGV1} CACHE STRING '"'Result from TRY_RUN'"' FORCE')')
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
    fi
}

addtask generate_toolchain_config_dir after do_generate_toolchain_file before do_configure

EXPORT_FUNCTIONS do_configure do_compile do_install do_generate_toolchain_config_dir