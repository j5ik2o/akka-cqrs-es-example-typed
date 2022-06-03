resource "eksctl_cluster" "primary" {
  eksctl_version = "0.38.0"
  name           = "primary1"
  region         = var.aws_region
  vpc_id         = module.vpc.vpc_id
  spec           = <<EOS

nodeGroups:
  - name: ng-1
    instanceType: t2.small
    desiredCapacity: 1

vpc:
  clusterEndpoint:
    privateAccess: true
    publicAccess: true
  cidr: "${module.vpc.vpc_cidr_block}"
  subnets:
    # must provide 'private' and/or 'public' subnets by availibility zone as shown
    private:
      ${module.vpc.azs[0]}:
        id: "${module.vpc.private_subnets[0]}"
        cidr: "${module.vpc.private_subnets_cidr_blocks[0]}" # (optional, must match CIDR used by the given subnet)
      ${module.vpc.azs[1]}:
        id: "${module.vpc.private_subnets[1]}"
        cidr: "${module.vpc.private_subnets_cidr_blocks[1]}"  # (optional, must match CIDR used by the given subnet)
      ${module.vpc.azs[2]}:
        id: "${module.vpc.private_subnets[2]}"
        cidr: "${module.vpc.private_subnets_cidr_blocks[2]}"   # (optional, must match CIDR used by the given subnet)
    public:
      ${module.vpc.azs[0]}:
        id: "${module.vpc.public_subnets[0]}"
        cidr: "${module.vpc.public_subnets_cidr_blocks[0]}" # (optional, must match CIDR used by the given subnet)
      ${module.vpc.azs[1]}:
        id: "${module.vpc.public_subnets[1]}"
        cidr: "${module.vpc.public_subnets_cidr_blocks[1]}"  # (optional, must match CIDR used by the given subnet)
      ${module.vpc.azs[2]}:
        id: "${module.vpc.public_subnets[2]}"
        cidr: "${module.vpc.public_subnets_cidr_blocks[2]}"   # (optional, must match CIDR used by the given subnet)

EOS

}