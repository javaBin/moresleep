
import * as cdk from 'aws-cdk-lib';
import { MoresleepStack } from '../lib/moresleep-stack.js';

const app = new cdk.App();
new MoresleepStack(app, 'moresleepstack', {
  env: { account: '553637109631', region: 'eu-central-1' },
});


