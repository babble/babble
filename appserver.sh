#!/bin/bash

. `dirname $0`/common.bash
$java_memory_small ed.appserver.AppServer "$@"
