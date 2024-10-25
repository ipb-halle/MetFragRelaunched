#usage awk -f split.awk filename
BEGIN {
    fileno = 1
}
{
    size += length()
}
size > 40000000 && /# SampleName =/ {
    fileno++
    size = 0
}
{
    print $0 > "chunk" fileno "_" ARGV[1];
}

