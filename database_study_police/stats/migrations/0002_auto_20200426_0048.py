# Generated by Django 3.0.3 on 2020-04-25 18:48

from django.db import migrations


class Migration(migrations.Migration):

    dependencies = [
        ('stats', '0001_initial'),
    ]

    operations = [
        migrations.RenameField(
            model_name='readingtime',
            old_name='user',
            new_name='student',
        ),
    ]
