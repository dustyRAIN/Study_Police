# Generated by Django 3.0.3 on 2020-05-05 09:15

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('stats', '0004_contentfrequency'),
    ]

    operations = [
        migrations.AddField(
            model_name='contentfrequency',
            name='name',
            field=models.CharField(default='no name', max_length=400),
            preserve_default=False,
        ),
    ]
