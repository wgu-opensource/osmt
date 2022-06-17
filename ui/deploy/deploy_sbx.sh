#deploy
cp deploy/env-sbx.js dist/osmt-ui/env.js
aws s3 cp --profile sbx --recursive dist/osmt-ui s3://osmt-ui-cloudfront-sbx/
