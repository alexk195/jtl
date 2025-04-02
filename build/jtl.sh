script_dir=$(dirname "$(readlink -f "$0")")
java -jar $script_dir/JTL.jar "$@"

