import React from 'react';
import clsx from 'clsx';
import styles from './HomepageFeatures.module.css';

const FeatureList = [
  {
    title: 'Ingest',
    Svg: require('../../static/img/icon-ingest.svg').default,
    description: (
      <>
        With Joyce you can easilly acquire data streams from any Kafka Connect compatible source or simply uploading a file with a REST API
      </>
    ),
  },
  {
    title: 'Transform',
    Svg: require('../../static/img/icon-transform.svg').default,
    description: (
      <>
        You can define through a json-schema based DSL how you want to ingest, clean enrich and store the incoming data 
      </>
    ),
  },
  {
    title: 'Expose',
    Svg: require('../../static/img/icon-expose.svg').default,
    description: (
      <>
        You get an automatic REST API to query the data
      </>
    ),
  },
];

function Feature({Svg, title, description}) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center box-icon">
        <Svg className={styles.featureSvg} alt={title} />
      </div>
      <div className="text--center padding-horiz--md">
        <h3>{title}</h3>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures() {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
