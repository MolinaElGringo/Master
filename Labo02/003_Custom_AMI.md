# Custom AMI and Deploy the second Drupal instance

In this task you will update your AMI with the Drupal settings and deploy it in the second availability zone.

## Task 01 - Create AMI

### [Create AMI](https://awscli.amazonaws.com/v2/documentation/api/latest/reference/ec2/create-image.html)

Note : stop the instance before

|Key|Value for GUI Only|
|:--|:--|
|Name|AMI_DRUPAL_DEVOPSTEAM[XX]_LABO02_RDS|
|Description|Same as name value|

```bash
[INPUT]
aws ec2 create-image --instance-id i-03d213d101ee4b7ca --name "AMI_DRUPAL_DEVOPSTEAM12_LABO02_RDS" --description "AMI_DRUPAL_DEVOPSTEAM12_LABO02_RDS" --tag-specifications 'ResourceType=image,Tags=[{Key="Name",Value="AMI_DRUPAL_DEVOPSTEAM12_LABO02_RDS"}]'

[OUTPUT]
{
    "ImageId": "ami-0b05698d9ed0f195f"
}
```

## Task 02 - Deploy Instances

* Restart Drupal Instance in Az1

* Deploy Drupal Instance based on AMI in Az2

|Key|Value for GUI Only|
|:--|:--|
|Name|EC2_PRIVATE_DRUPAL_DEVOPSTEAM[XX]_B|
|Description|Same as name value|

```bash
[INPUT]
aws ec2 run-instances --image-id ami-0b05698d9ed0f195f --count 1 --instance-type t3.micro --key-name CLD_KEY_DRUPAL_DEVOPSTEAM12 --private-ip-address 10.0.12.140 --security-group-ids sg-09b5635164d56ffa8 --subnet-id subnet-021be5af8872e3461 --tag-specifications "ResourceType=instance,Tags=[{Key='Name',Value='EC2_PRIVATE_DRUPAL_DEVOPSTEAM12_B'}]"

[OUTPUT]
{
    "Groups": [],
    "Instances": [
        {
            "AmiLaunchIndex": 0,
            "ImageId": "ami-0b05698d9ed0f195f",
            "InstanceId": "i-0c9369b3645ba4493",
            "InstanceType": "t3.micro",
            "KeyName": "CLD_KEY_DRUPAL_DEVOPSTEAM12",
            "LaunchTime": "2024-03-26T14:29:40+00:00",
            "Monitoring": {
                "State": "disabled"
            },
            "Placement": {
                "AvailabilityZone": "eu-west-3b",
                "GroupName": "",
                "Tenancy": "default"
            },
            "PrivateDnsName": "ip-10-0-12-140.eu-west-3.compute.internal",
            "PrivateIpAddress": "10.0.12.140",
            "ProductCodes": [],
            "PublicDnsName": "",
            "State": {
                "Code": 0,
                "Name": "pending"
            },
            "StateTransitionReason": "",
            "SubnetId": "subnet-021be5af8872e3461",
            "VpcId": "vpc-03d46c285a2af77ba",
            "Architecture": "x86_64",
            "BlockDeviceMappings": [],
            "ClientToken": "d6e2975a-2b35-44c1-933d-576fb50687db",
            "EbsOptimized": false,
            "EnaSupport": true,
            "Hypervisor": "xen",
            "NetworkInterfaces": [
                {
                    "Attachment": {
                        "AttachTime": "2024-03-26T14:29:40+00:00",
                        "AttachmentId": "eni-attach-0430d2738fa8af8a0",
                        "DeleteOnTermination": true,
                        "DeviceIndex": 0,
                        "Status": "attaching",
                        "NetworkCardIndex": 0
                    },
                    "Description": "",
                    "Groups": [
                        {
                            "GroupName": "SG-PRIVATE-DRUPAL-DEVOPSTEAM12",
                            "GroupId": "sg-09b5635164d56ffa8"
                        }
                    ],
                    "Ipv6Addresses": [],
                    "MacAddress": "0a:e1:bf:fa:30:63",
                    "NetworkInterfaceId": "eni-0315b7737cd406113",
                    "OwnerId": "709024702237",
                    "PrivateIpAddress": "10.0.12.140",
                    "PrivateIpAddresses": [
                        {
                            "Primary": true,
                            "PrivateIpAddress": "10.0.12.140"
                        }
                    ],
                    "SourceDestCheck": true,
                    "Status": "in-use",
                    "SubnetId": "subnet-021be5af8872e3461",
                    "VpcId": "vpc-03d46c285a2af77ba",
                    "InterfaceType": "interface"
                }
            ],
            "RootDeviceName": "/dev/xvda",
            "RootDeviceType": "ebs",
            "SecurityGroups": [
                {
                    "GroupName": "SG-PRIVATE-DRUPAL-DEVOPSTEAM12",
                    "GroupId": "sg-09b5635164d56ffa8"
                }
            ],
            "SourceDestCheck": true,
            "StateReason": {
                "Code": "pending",
                "Message": "pending"
            },
            "Tags": [
                {
                    "Key": "Name",
                    "Value": "EC2_PRIVATE_DRUPAL_DEVOPSTEAM12_B"
                }
            ],
            "VirtualizationType": "hvm",
            "CpuOptions": {
                "CoreCount": 1,
                "ThreadsPerCore": 2
            },
            "CapacityReservationSpecification": {
                "CapacityReservationPreference": "open"
            },
            "MetadataOptions": {
                "State": "pending",
                "HttpTokens": "optional",
                "HttpPutResponseHopLimit": 1,
                "HttpEndpoint": "enabled",
                "HttpProtocolIpv6": "disabled",
                "InstanceMetadataTags": "disabled"
            },
            "EnclaveOptions": {
                "Enabled": false
            },
            "PrivateDnsNameOptions": {
                "HostnameType": "ip-name",
                "EnableResourceNameDnsARecord": false,
                "EnableResourceNameDnsAAAARecord": false
            },
            "MaintenanceOptions": {
                "AutoRecovery": "default"
            },
            "CurrentInstanceBootMode": "legacy-bios"
        }
    ],
    "OwnerId": "709024702237",
    "ReservationId": "r-0ce4fc9a44b49fe3e"
}
```

## Task 03 - Test the connectivity

### Update your ssh connection string to test

* add tunnels for ssh and http pointing on the B Instance

```bash
ssh devopsteam12@15.188.43.46 -i CLD_KEY_DMZ_DEVOPSTEAM12.pem -L 2224:10.0.12.140:22 -L 2225:10.0.12.7:22 -L 8081:10.0.12.140:8080 -L 8082:10.0.12.7:8080
```

## Check SQL Accesses

```sql
[INPUT]
//sql string connection from A
mysql -h dbi-devopsteam12.cshki92s4w5p.eu-west-3.rds.amazonaws.com -u bn_drupal --password=2b9defd18a354804a1d4c4742c252fb39d808c12cfc2046ffc8f31432ae8a060

[OUTPUT]
mysql: Deprecated program name. It will be removed in a future release, use '/opt/bitnami/mariadb/bin/mariadb' instead
Welcome to the MariaDB monitor.  Commands end with ; or \g.
Your MariaDB connection id is 8457
Server version: 10.11.7-MariaDB managed by https://aws.amazon.com/rds/

Copyright (c) 2000, 2018, Oracle, MariaDB Corporation Ab and others.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

MariaDB [(none)]> show databases;
+--------------------+
| Database           |
+--------------------+
| bitnami_drupal     |
| information_schema |
+--------------------+
2 rows in set (0.001 sec)

MariaDB [(none)]>
```

```sql
[INPUT]
//sql string connection from B
mysql -h dbi-devopsteam12.cshki92s4w5p.eu-west-3.rds.amazonaws.com -u bn_drupal --password=2b9defd18a354804a1d4c4742c252fb39d808c12cfc2046ffc8f31432ae8a060

[OUTPUT]
mysql: Deprecated program name. It will be removed in a future release, use '/opt/bitnami/mariadb/bin/mariadb' instead
Welcome to the MariaDB monitor.  Commands end with ; or \g.
Your MariaDB connection id is 8484
Server version: 10.11.7-MariaDB managed by https://aws.amazon.com/rds/

Copyright (c) 2000, 2018, Oracle, MariaDB Corporation Ab and others.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

MariaDB [(none)]> show databases;
+--------------------+
| Database           |
+--------------------+
| bitnami_drupal     |
| information_schema |
+--------------------+
2 rows in set (0.001 sec)

MariaDB [(none)]>
```

### Check HTTP Accesses

```bash
//connection string updated
ssh devopsteam12@15.188.43.46 -i CLD_KEY_DMZ_DEVOPSTEAM12.pem -L 2224:10.0.12.140:22 -L 2225:10.0.12.7:22 -L 8081:10.0.12.140:8080 -L 8082:10.0.12.7:8080

//check the webapp
http://localhost:8081
http://localhost:8082

//or using curl
curl http://localhost:8081
curl http://localhost:8082

```

### Read and write test through the web app

* Login in both webapps (same login)

* Change the users' email address on a webapp... refresh the user's profile page on the second and validated that they are communicating with the same db (rds).

* Observations ?

```
To do this, we had to retrieve the username and password using sudo cat /home/bitnami/bitnami_credentials.

Username: users

Password: ...

When we logged into instance A via the webapp, we were also logged into instance B.

We changed the email address on instance A and the one on B was also changed. 
We can therefore assert that both servers are connected to the same database.
```

### Change the profil picture

* Observations ?

```
The profile picture is not the same on both instances. This is because the images are stored in the instance's file system and not in the database. The images are not synchronized between the two instances.
In one instance, the image is visible, while in the other, it is not.
```