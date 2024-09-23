## Requirements

You must have these installed prior to running the pipeline
- java 11
- maven >=3.3
- terraform >=1.0
- bash
- docker

## Configuration

you can configure the pipeline using environment variables, by setting vars in `scripts/envrc.bash` and then running `source scripts/envrc.bash`

## Running the Pipeline

```
chmod +x scripts/*
./scripts/build-docker-images.sh
./scripts/bootstrap-infra.sh
```

## Debugging and inspection
**TBD**

## Cleaning up
```
./scripts/bootstrap-infra.sh destroy
```
