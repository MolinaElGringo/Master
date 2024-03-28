### Deploy the elastic load balancer

In this task you will create a load balancer in AWS that will receive
the HTTP requests from clients and forward them to the Drupal
instances.

![Schema](./img/CLD_AWS_INFA.PNG)

## Task 01 Prerequisites for the ELB

* Create a dedicated security group

|Key|Value|
|:--|:--|
|Name|SG-DEVOPSTEAM[XX]-LB|
|Inbound Rules|Application Load Balancer|
|Outbound Rules|Refer to the infra schema|

```bash
[INPUT]
aws ec2 create-security-group --group-name SG-DEVOPSTEAM12-LB --description "SG-DEVOPSTEAM12-LB" --vpc-id vpc-03d46c285a2af77ba --tag-specifications ResourceType=security-group,Tags=[{Key=Name,Value=SG-DEVOPSTEAM12-LB}]

[OUTPUT]
{
    "GroupId": "sg-05ae6b0825ffbda13",
    "Tags": [
        {
            "Key": "Name",
            "Value": "SG-DEVOPSTEAM12-LB"
        }
    ]
}
```

* Create the authorization for the security group

```bash
[INPUT]
aws ec2 authorize-security-group-ingress --group-id sg-05ae6b0825ffbda13 --ip-permissions IpProtocol=tcp,FromPort=8080,ToPort=8080,IpRanges="[{CidrIp=0.0.0.0/0,Description='HTTP'}]"


[OUTPUT]
{
    "Return": true,
    "SecurityGroupRules": [
        {
            "SecurityGroupRuleId": "sgr-0998d1ff543ffcc48",
            "GroupId": "sg-05ae6b0825ffbda13",
            "GroupOwnerId": "709024702237",
            "IsEgress": false,
            "IpProtocol": "tcp",
            "FromPort": 8080,
            "ToPort": 8080,
            "CidrIpv4": "0.0.0.0/0",
            "Description": "HTTP"
        }
    ]
}
```

* Create the Target Group

|Key|Value|
|:--|:--|
|Target type|Instances|
|Name|TG-DEVOPSTEAM[XX]|
|Protocol and port|Refer to the infra schema|
|Ip Address type|IPv4|
|VPC|Refer to the infra schema|
|Protocol version|HTTP1|
|Health check protocol|HTTP|
|Health check path|/|
|Port|Traffic port|
|Healthy threshold|2 consecutive health check successes|
|Unhealthy threshold|2 consecutive health check failures|
|Timeout|5 seconds|
|Interval|10 seconds|
|Success codes|200|

```bash
[INPUT]
aws elbv2 create-target-group --name TG-DEVOPSTEAM12 --protocol HTTP --protocol-version HTTP1 --port 8080 --ip-address-type ipv4 --vpc-id vpc-03d46c285a2af77ba --healthy-threshold-count 2 --unhealthy-threshold-count 2 --health-check-timeout-seconds 5 --health-check-interval-seconds 10 --matcher HttpCode=200

[OUTPUT]
{
    "TargetGroups": [
        {
            "TargetGroupArn": "arn:aws:elasticloadbalancing:eu-west-3:709024702237:targetgroup/TG-DEVOPSTEAM12/8db23a65eb5e2be2",
            "TargetGroupName": "TG-DEVOPSTEAM12",
            "Protocol": "HTTP",
            "Port": 8080,
            "VpcId": "vpc-03d46c285a2af77ba",
            "HealthCheckProtocol": "HTTP",
            "HealthCheckPort": "traffic-port",
            "HealthCheckEnabled": true,
            "HealthCheckIntervalSeconds": 10,
            "HealthCheckTimeoutSeconds": 5,
            "HealthyThresholdCount": 2,
            "UnhealthyThresholdCount": 2,
            "HealthCheckPath": "/",
            "Matcher": {
                "HttpCode": "200"
            },
            "TargetType": "instance",
            "ProtocolVersion": "HTTP1",
            "IpAddressType": "ipv4"
        }
    ]
}
```

* Register the instances in the target group

```bash
[INPUT]
aws elbv2 register-targets --target-group-arn arn:aws:elasticloadbalancing:eu-west-3:709024702237:targetgroup/TG-DEVOPSTEAM12/8db23a65eb5e2be2 --targets Id=i-03d213d101ee4b7ca Id=i-0c9369b3645ba4493
```


## Task 02 Deploy the Load Balancer

[Source](https://aws.amazon.com/elasticloadbalancing/)

* Create the Load Balancer

|Key|Value|
|:--|:--|
|Type|Application Load Balancer|
|Name|ELB-DEVOPSTEAM99|
|Scheme|Internal|
|Ip Address type|IPv4|
|VPC|Refer to the infra schema|
|Security group|Refer to the infra schema|
|Listeners Protocol and port|Refer to the infra schema|
|Target group|Your own target group created in task 01|

Provide the following answers (leave any
field not mentioned at its default value):

```bash
[INPUT]
aws elbv2 create-load-balancer --name ELB-DEVOPSTEAM12 --scheme internal --security-groups sg-05ae6b0825ffbda13 --subnets subnet-056fea5868b18075a subnet-021be5af8872e3461

[OUTPUT]
{
    "LoadBalancers": [
        {
            "LoadBalancerArn": "arn:aws:elasticloadbalancing:eu-west-3:709024702237:loadbalancer/app/ELB-DEVOPSTEAM12/d6027499d37778fd",
            "DNSName": "internal-ELB-DEVOPSTEAM12-99666020.eu-west-3.elb.amazonaws.com",
            "CanonicalHostedZoneId": "Z3Q77PNBQS71R4",
            "CreatedTime": "2024-03-27T21:29:31.080000+00:00",
            "LoadBalancerName": "ELB-DEVOPSTEAM12",
            "Scheme": "internal",
            "VpcId": "vpc-03d46c285a2af77ba",
            "State": {
                "Code": "provisioning"
            },
            "Type": "application",
            "AvailabilityZones": [
                {
                    "ZoneName": "eu-west-3b",
                    "SubnetId": "subnet-021be5af8872e3461",
                    "LoadBalancerAddresses": []
                },
                {
                    "ZoneName": "eu-west-3a",
                    "SubnetId": "subnet-056fea5868b18075a",
                    "LoadBalancerAddresses": []
                }
            ],
            "SecurityGroups": [
                "sg-05ae6b0825ffbda13"
            ],
            "IpAddressType": "ipv4"
        }
    ]
}
```

* Create the listener for the load balancer

```bash
[INPUT]
aws elbv2 create-listener --load-balancer-arn arn:aws:elasticloadbalancing:eu-west-3:709024702237:loadbalancer/app/ELB-DEVOPSTEAM12/d6027499d37778fd --protocol HTTP --port 8080 --default-actions Type=forward,TargetGroupArn=arn:aws:elasticloadbalancing:eu-west-3:709024702237:targetgroup/TG-DEVOPSTEAM12/8db23a65eb5e2be2

[OUTPUT]
{
    "Listeners": [
        {
            "ListenerArn": "arn:aws:elasticloadbalancing:eu-west-3:709024702237:listener/app/ELB-DEVOPSTEAM12/d6027499d37778fd/d5b2e62e37b75153",
            "LoadBalancerArn": "arn:aws:elasticloadbalancing:eu-west-3:709024702237:loadbalancer/app/ELB-DEVOPSTEAM12/d6027499d37778fd",
            "Port": 8080,
            "Protocol": "HTTP",
            "DefaultActions": [
                {
                    "Type": "forward",
                    "TargetGroupArn": "arn:aws:elasticloadbalancing:eu-west-3:709024702237:targetgroup/TG-DEVOPSTEAM12/8db23a65eb5e2be2",
                    "ForwardConfig": {
                        "TargetGroups": [
                            {
                                "TargetGroupArn": "arn:aws:elasticloadbalancing:eu-west-3:709024702237:targetgroup/TG-DEVOPSTEAM12/8db23a65eb5e2be2",
                                "Weight": 1
                            }
                        ],
                        "TargetGroupStickinessConfig": {
                            "Enabled": false
                        }
                    }
                }
            ]
        }
    ]
}
```

We then need to update the security group of the instances to allow the ELB to communicate with them. We will add a rule to allow the ELB to communicate with the instances on port 8080.

### Subnet A:

```bash
[INPUT]
aws ec2 authorize-security-group-ingress --group-id sg-09b5635164d56ffa8 --ip-permissions IpProtocol=tcp,FromPort=8080,ToPort=8080,IpRanges="[{CidrIp=10.0.12.0/28}]"

[OUTPUT]
{
    "Return": true,
    "SecurityGroupRules": [
        {
            "SecurityGroupRuleId": "sgr-03200b10455aa370b",
            "GroupId": "sg-09b5635164d56ffa8",
            "GroupOwnerId": "709024702237",
            "IsEgress": false,
            "IpProtocol": "tcp",
            "FromPort": 8080,
            "ToPort": 8080,
            "CidrIpv4": "10.0.12.0/28"
        }
    ]
}

```

### Subnet B:

```bash
[INPUT]
aws ec2 authorize-security-group-ingress --group-id sg-09b5635164d56ffa8 --ip-permissions IpProtocol=tcp,FromPort=8080,ToPort=8080,IpRanges="[{CidrIp=10.0.12.128/28}]"

[OUTPUT]
{
    "Return": true,
    "SecurityGroupRules": [
        {
            "SecurityGroupRuleId": "sgr-0d54bf3b579f0e363",
            "GroupId": "sg-09b5635164d56ffa8",
            "GroupOwnerId": "709024702237",
            "IsEgress": false,
            "IpProtocol": "tcp",
            "FromPort": 8080,
            "ToPort": 8080,
            "CidrIpv4": "10.0.12.128/28"
        }
    ]
}
```

* Get the ELB FQDN (DNS NAME - A Record)

```bash
[INPUT]
aws elbv2 describe-load-balancers --name ELB-DEVOPSTEAM12

[OUTPUT]
{
    "LoadBalancers": [
        {
            "LoadBalancerArn": "arn:aws:elasticloadbalancing:eu-west-3:709024702237:loadbalancer/app/ELB-DEVOPSTEAM12/d6027499d37778fd",
            "DNSName": "internal-ELB-DEVOPSTEAM12-99666020.eu-west-3.elb.amazonaws.com",
            "CanonicalHostedZoneId": "Z3Q77PNBQS71R4",
            "CreatedTime": "2024-03-27T21:29:31.080000+00:00",
            "LoadBalancerName": "ELB-DEVOPSTEAM12",
            "Scheme": "internal",
            "VpcId": "vpc-03d46c285a2af77ba",
            "State": {
                "Code": "active"
            },
            "Type": "application",
            "AvailabilityZones": [
                {
                    "ZoneName": "eu-west-3b",
                    "SubnetId": "subnet-021be5af8872e3461",
                    "LoadBalancerAddresses": []
                },
                {
                    "ZoneName": "eu-west-3a",
                    "SubnetId": "subnet-056fea5868b18075a",
                    "LoadBalancerAddresses": []
                }
            ],
            "SecurityGroups": [
                "sg-05ae6b0825ffbda13"
            ],
            "IpAddressType": "ipv4"
        }
    ]
}
```

* Get the ELB deployment status

Note : In the EC2 console select the Target Group. In the
       lower half of the panel, click on the **Targets** tab. Watch the
       status of the instance go from **unused** to **initial**.

* Ask the DMZ administrator to register your ELB with the reverse proxy via the private teams channel

* Update your string connection to test your ELB and test it

```bash
//connection string updated
ssh devopsteam12@15.188.43.46 -i CLD_KEY_DMZ_DEVOPSTEAM12.pem -L 2224:internal-ELB-DEVOPSTEAM12-99666020.eu-west-3.elb.amazonaws.com:8080
```

* Test your application through your ssh tunneling

```bash
[INPUT]
curl localhost:2224

[OUTPUT]
StatusCode        : 200
StatusDescription : OK
Content           : <!DOCTYPE html>
                    <html lang="en" dir="ltr" style="--color--primary-hue:202;--color--primary-satura
                    tion:79%;--color--primary-lightness:50">
                      <head>
                        <meta charset="utf-8" />
                    <meta name="Generator" c...
RawContent        : HTTP/1.1 200 OK
                    Connection: keep-alive
                    X-Drupal-Dynamic-Cache: MISS
                    Content-language: en
                    X-Content-Type-Options: nosniff
                    X-Frame-Options: SAMEORIGIN
                    X-Generator: Drupal 10 (https://www.drupal.or...
Forms             : {search-block-form, search-block-form--2}
Headers           : {[Connection, keep-alive], [X-Drupal-Dynamic-Cache, MISS], [Content-language,
                    en], [X-Content-Type-Options, nosniff]...}
Images            : {}
InputFields       : {@{innerHTML=; innerText=; outerHTML=<INPUT id=edit-keys title="Enter the terms
                    you wish to search for." class="form-search form-element
                    form-element--type-search form-element--api-search" maxLength=128 size=15
                    name=keys data-drupal-selector="edit-keys" placeholder="Search by keyword or
                    phrase.">; outerText=; tagName=INPUT; id=edit-keys; title=Enter the terms you
                    wish to search for.; class=form-search form-element form-element--type-search
                    form-element--api-search; maxLength=128; size=15; name=keys;
                    data-drupal-selector=edit-keys; placeholder=Search by keyword or phrase.},
                    @{innerHTML=; innerText=; outerHTML=<INPUT id=edit-keys--2 title="Enter the
                    terms you wish to search for." class="form-search form-element
                    form-element--type-search form-element--api-search" maxLength=128 size=15
                    name=keys data-drupal-selector="edit-keys" placeholder="Search by keyword or
                    phrase.">; outerText=; tagName=INPUT; id=edit-keys--2; title=Enter the terms you
                    wish to search for.; class=form-search form-element form-element--type-search
                    form-element--api-search; maxLength=128; size=15; name=keys;
                    data-drupal-selector=edit-keys; placeholder=Search by keyword or phrase.}}
Links             : {@{innerHTML=Skip to main content ; innerText=Skip to main content ;
                    outerHTML=<A class="visually-hidden focusable skip-link"
                    href="#main-content">Skip to main content </A>; outerText=Skip to main content ;
                    tagName=A; class=visually-hidden focusable skip-link; href=#main-content},
                    @{innerHTML=My blog; innerText=My blog; outerHTML=<A title=Home href="/"
                    rel=home>My blog</A>; outerText=My blog; tagName=A; title=Home; href=/;
                    rel=home}, @{innerHTML=<SPAN class="primary-nav__menu-link-inner
                    primary-nav__menu-link-inner--level-1">Home</SPAN> ; innerText=Home ;
                    outerHTML=<A class="primary-nav__menu-link primary-nav__menu-link--link
                    primary-nav__menu-link--level-1 is-active" href="/"
                    data-drupal-selector="primary-nav-menu-link-has-children"
                    data-drupal-link-system-path="<front>"><SPAN class="primary-nav__menu-link-inner
                    primary-nav__menu-link-inner--level-1">Home</SPAN> </A>; outerText=Home ;
                    tagName=A; class=primary-nav__menu-link primary-nav__menu-link--link
                    primary-nav__menu-link--level-1 is-active; href=/;
                    data-drupal-selector=primary-nav-menu-link-has-children;
                    data-drupal-link-system-path=<front>}, @{innerHTML=Log in; innerText=Log in;
                    outerHTML=<A class="secondary-nav__menu-link secondary-nav__menu-link--link
                    secondary-nav__menu-link--level-1" href="/user/login"
                    data-drupal-link-system-path="user/login">Log in</A>; outerText=Log in;
                    tagName=A; class=secondary-nav__menu-link secondary-nav__menu-link--link
                    secondary-nav__menu-link--level-1; href=/user/login;
                    data-drupal-link-system-path=user/login}...}
ParsedHtml        : mshtml.HTMLDocumentClass
RawContentLength  : 16554
```

#### Questions - Analysis

* On your local machine resolve the DNS name of the load balancer into
  an IP address using the `nslookup` command (works on Linux, macOS and Windows). Write
  the DNS name and the resolved IP Address(es) into the report.

```
Serveur :   UnKnown
Address:  192.168.0.1

Réponse ne faisant pas autorité :
Nom :    internal-ELB-DEVOPSTEAM12-99666020.eu-west-3.elb.amazonaws.com
Addresses:  10.0.12.141
            10.0.12.6
```

* From your Drupal instance, identify the ip from which requests are sent by the Load Balancer.

Help : execute `tcpdump port 8080`

```
bitnami@ip-10-0-12-140:~$ sudo tcpdump port 8080
tcpdump: verbose output suppressed, use -v[v]... for full protocol decode
listening on ens5, link-type EN10MB (Ethernet), snapshot length 262144 bytes
23:40:07.790717 IP 10.0.12.141.35960 > 10.0.12.140.http-alt: Flags [S], seq 2838042316, win 26883, options [mss 8961,sackOK,TS val 1178851000 ecr 0,nop,wscale 8], length 0
23:40:07.790740 IP 10.0.12.140.http-alt > 10.0.12.141.35960: Flags [S.], seq 4168879457, ack 2838042317, win 62643, options [mss 8961,sackOK,TS val 4050280577 ecr 1178851000,nop,wscale 7], length 0
23:40:07.791258 IP 10.0.12.141.35960 > 10.0.12.140.http-alt: Flags [.], ack 1, win 106, options [nop,nop,TS val 1178851000 ecr 4050280577], length 0
23:40:07.791259 IP 10.0.12.141.35960 > 10.0.12.140.http-alt: Flags [P.], seq 1:132, ack 1, win 106, options [nop,nop,TS val 1178851000 ecr 4050280577], length 131: HTTP: GET / HTTP/1.1
23:40:07.791277 IP 10.0.12.140.http-alt > 10.0.12.141.35960: Flags [.], ack 132, win 489, options [nop,nop,TS val 4050280578 ecr 1178851000], length 0
23:40:07.815102 IP 10.0.12.140.http-alt > 10.0.12.141.35960: Flags [P.], seq 1:5624, ack 132, win 489, options [nop,nop,TS val 4050280602 ecr 1178851000], length 5623: HTTP: HTTP/1.1 200 OK
23:40:07.815380 IP 10.0.12.140.http-alt > 10.0.12.141.35960: Flags [F.], seq 5624, ack 132, win 489, options [nop,nop,TS val 4050280602 ecr 1178851000], length 0
23:40:07.815624 IP 10.0.12.141.35960 > 10.0.12.140.http-alt: Flags [.], ack 5624, win 175, options [nop,nop,TS val 1178851025 ecr 4050280602], length 0
23:40:07.815683 IP 10.0.12.141.35960 > 10.0.12.140.http-alt: Flags [F.], seq 132, ack 5624, win 175, options [nop,nop,TS val 1178851025 ecr 4050280602], length 0
23:40:07.815685 IP 10.0.12.140.http-alt > 10.0.12.141.35960: Flags [.], ack 133, win 489, options [nop,nop,TS val 4050280602 ecr 1178851025], length 0
23:40:07.815877 IP 10.0.12.141.35960 > 10.0.12.140.http-alt: Flags [.], ack 5625, win 175, options [nop,nop,TS val 1178851025 ecr 4050280602], length 0
^C
11 packets captured
11 packets received by filter
0 packets dropped by kernel
bitnami@ip-10-0-12-140:~$
```

* In the Apache access log identify the health check accesses from the
  load balancer and copy some samples into the report.

```
tail -n 10 /opt/bitnami/apache2/logs/access_log
10.0.12.141 - - [27/Mar/2024:23:43:17 +0000] "GET / HTTP/1.1" 200 5148
10.0.12.6 - - [27/Mar/2024:23:43:19 +0000] "GET / HTTP/1.1" 200 5148
10.0.12.141 - - [27/Mar/2024:23:43:27 +0000] "GET / HTTP/1.1" 200 5148
10.0.12.6 - - [27/Mar/2024:23:43:29 +0000] "GET / HTTP/1.1" 200 5148
10.0.12.141 - - [27/Mar/2024:23:43:37 +0000] "GET / HTTP/1.1" 200 5148
10.0.12.6 - - [27/Mar/2024:23:43:39 +0000] "GET / HTTP/1.1" 200 5148
10.0.12.141 - - [27/Mar/2024:23:43:47 +0000] "GET / HTTP/1.1" 200 5148
10.0.12.6 - - [27/Mar/2024:23:43:49 +0000] "GET / HTTP/1.1" 200 5148
10.0.12.141 - - [27/Mar/2024:23:43:57 +0000] "GET / HTTP/1.1" 200 5148
10.0.12.6 - - [27/Mar/2024:23:43:59 +0000] "GET / HTTP/1.1" 200 5148
```
